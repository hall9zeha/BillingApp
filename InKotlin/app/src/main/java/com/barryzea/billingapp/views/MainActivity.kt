package com.barryzea.billingapp.views

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.barryzea.billingapp.MyApp
import com.barryzea.billingapp.R
import com.barryzea.billingapp.common.Constants.PREMIUM_ID
import com.barryzea.billingapp.common.Constants.REMOVE_ADS_ID
import com.barryzea.billingapp.databinding.ActivityMainBinding
import com.google.common.collect.ImmutableList

class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {
    private lateinit var bind: ActivityMainBinding
    private var billingClient: BillingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind= ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        doMyBiller()
        setUpListeners()
        setMessageStatusOfPurchase()

    }

    private fun setMessageStatusOfPurchase() {
        //Llenamos el textView correspondiente con un mensaje si hay productos comprados o no
        //o podemos realizar cualquier evento relacionado con la compra de los productos dentro de la app
        //tales como desactivar los anuncios o habilitar funciones para usuario premium
        //así como dejar de llamar a los productos.
        if (MyApp.prefs.buyRemoveAds && MyApp.prefs.buyPremiumUser) {
            bind.tvHello.setText(R.string.allPaidMsg)}
        else if (MyApp.prefs.buyRemoveAds) {
            bind.tvHello.setText(R.string.removeAdsPaid)
        } else if (MyApp.prefs.buyPremiumUser) {
            bind.tvHello.setText(R.string.premiumUserPaid)
        }
    }

    private fun setUpListeners() {
        bind.btnPurchase.setOnClickListener {
            //si no cargan los productos al inicio los llamamos en este botón
            showProducts()
        }
    }

    private fun doMyBiller() {
        //inicializamos nuestro cliente de compras
        billingClient = BillingClient.newBuilder(this@MainActivity)
            .enablePendingPurchases()
            .setListener(this).build()
        //podríamos llamar aquí también a la función establishConnection() pero ya lo hacemos en onStart:
        // establishConnection()
    }

    private fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                //Si hubiera un error de desconexión volvemos a ejecutar la función
                establishConnection()
                Log.e("ON_DISCONECT", "desconectado")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    //si la conexión es exitosa consultamos los productos de la app desde google play
                    //y los mostramos
                    if (!MyApp.prefs.buyRemoveAds) {
                        showProducts()
                    }
                    Log.d("ON_FINISH", "Conectado")
                }
            }
        })
    }

    private fun showProducts() {
        //Primero creamos una lista con los mismos productos que tenemos creados en play console para nuestra app
        val productList = ImmutableList.of<Product>(
            Product.newBuilder()
                .setProductId(REMOVE_ADS_ID) //REMOVE_ADS_ID viene de la clase Constants
                .setProductType(BillingClient.ProductType.INAPP) //.setProductType(BillingClient.ProductType.SUBS)//el en caso de tener suscripciones usar esta forma
                .build(),
            Product.newBuilder()
                .setProductId(PREMIUM_ID) //PREMIUM_ID viene de la clase Constants
                .setProductType(BillingClient.ProductType.INAPP) //.setProductType(BillingClient.ProductType.SUBS)//el en caso de tener suscripciones usar esta forma
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        //ahora escuchamos la respuesta y si nos trae una confirmación de productos existentes manejamos lo siguiente:
        billingClient!!.queryProductDetailsAsync(
            params
        ) { billingResult, productDetails ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (productDetails.isNotEmpty()) {
                    productDetails.forEach {detail->
                        //comparamos si el primer elemento de la lista traida desde nuestra cuenta de google
                        //es igual al id del producto
                        if (detail.productId == REMOVE_ADS_ID) {
                            //comprobamos si en nuestras preferencias guardadas la compra de este producto
                            //esta en falso para así mostrar las vistas del producto, caso contrario no se mostrará
                            if (!MyApp.prefs.buyRemoveAds) {
                                //List<ProductDetails.SubscriptionOfferDetails> subDetails = detail.getSubscriptionOfferDetails(); si nuestra lista tiene suscripciones usar esta forma  para acceder a los detalles
                                runOnUiThread {
                                    //llenamos las vistas con la descripción y el precio de nuestro producto
                                    bind.purchaseCard.visibility = View.VISIBLE
                                    bind.tvDescription.text = detail.description

                                    //para obtener los detalles de un producto INAPP o de una sola compra
                                    //se hace con getOneTimePurchaseOfferDetails()
                                    bind.tvPrice.text =
                                        detail.oneTimePurchaseOfferDetails!!.formattedPrice

                                    //para obtener los detalles de un producto SUBS o suscripción:
                                    //bind.tvPrice.setText(subDetails.get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice()); si fuera una suscripción usar esta forma
                                    bind.purchaseCard.setOnClickListener {
                                        //iniciamos el evento click de cualquier elemento que designemos para ello
                                        //enviándole los detalles del producto cargado para que se muestre el mensaje de compra
                                        launchPurchaseFlow(detail)
                                    }
                                }
                            }
                        }
                        //lo mismo si hubiera un segundo elemento en nuestra lista de productos
                        if (detail.productId == PREMIUM_ID) {
                            //comprobamos si en nuestras preferencias guardadas la compra de este producto
                            //esta en falso para así mostrar las vistas del producto, caso contrario no se mostrará
                            if (!MyApp.prefs.buyPremiumUser) {
                                runOnUiThread {
                                    bind.premiumCard.visibility = View.VISIBLE
                                    bind.tvDescription1.text = detail.description
                                    bind.tvPrice1.text = detail.oneTimePurchaseOfferDetails!!.formattedPrice
                                    bind.premiumCard.setOnClickListener {launchPurchaseFlow( detail)}
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity,"No se encontró el ítem de producto",Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "Error " + billingResult.debugMessage,Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        //en este caso al ser un producto de una sola compra y no suscripción usamos el método getOneTimePurchaseOfferDetails()
        assert(productDetails.oneTimePurchaseOfferDetails != null)
        val productDetailsParamsList: ImmutableList<ProductDetailsParams> = ImmutableList.of(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails) //Esta parte solo es necesaria para suscripciones
                //.setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        //listo para lanzar el diálogo de compra de cada elemento cargado en nuestras vistas
        val billingResult = billingClient!!.launchBillingFlow(this@MainActivity, billingFlowParams)
    }

    private fun verifyPurchase(purchases: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchases.purchaseToken)
            .build()
        billingClient!!.acknowledgePurchase(
            acknowledgePurchaseParams
        ) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //Ahora que la compra ha sido confirmada podemos  ejecutar el código que sea conveniente para nosotros
                //En este caso al ser una compra para quitar los anuncios de la app, se guardará un valor en preferencias
                //para desactivar los anuncios
                when (purchases.products[0]) {
                    REMOVE_ADS_ID -> {
                        //en el caso de que se haya comprado: desactivar anuncios, hacer los siquiente:
                        MyApp.prefs.buyRemoveAds = true
                        if (MyApp.prefs.buyRemoveAds) {
                            bind.purchaseCard.visibility = View.GONE
                            setMessageStatusOfPurchase()
                            Toast.makeText(this, R.string.successfulPurchase, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    PREMIUM_ID -> {
                        //en el caso de haber comprado usuario premium lo siguiente:
                        MyApp.prefs.buyPremiumUser = true
                        if (MyApp.prefs.buyPremiumUser) {
                            bind.premiumCard.visibility = View.GONE
                            setMessageStatusOfPurchase()
                            Toast.makeText(this, R.string.successfulPurchase, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
        Log.d("TAG", "Purchase Token: " + purchases.purchaseToken)
        Log.d("TAG", "Purchase Time: " + purchases.purchaseTime)
        Log.d("TAG", "Purchase OrderID: " + purchases.orderId)
    }


    private fun checkPurchases() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient!!

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP) //.setProductType(BillingClient.ProductType.SUBS) para suscripciones
                            .build()
                    ) { billingResult1: BillingResult, list: List<Purchase> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d("TEST_PRODUCTS_SIZE", list.size.toString() + " size")
                            if (list.isNotEmpty()) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Compras verificadas",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                //si tenemos un sólo producto no tenemos que revisar la lista con un ciclo
                                //MyApp.prefs.setBuyRemoveAds(true) guardamos el estado sencillamente aquí
                                list.forEach {purchase->
                                    //si la devolución de productos comprados en este dispositivo tiene multiples suscripciones o varios productos de una sola compra
                                    //las revisamos aquí
                                    when (purchase.products[0]) {
                                        REMOVE_ADS_ID -> MyApp.prefs.buyRemoveAds = true
                                        PREMIUM_ID -> MyApp.prefs.buyPremiumUser = true
                                    }
                                }
                            } else {
                                MyApp.prefs.buyRemoveAds = false
                                MyApp.prefs.buyPremiumUser = false
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchaseList: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList != null) {
            purchaseList.forEach{purchase->
                //si se ha presionado en comprar producto dentro del dialog de compras, verificamos
                verifyPurchase(purchase!!)
            }
        } else if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.USER_CANCELED
        ) {
            Log.i("CANCELED", "onPurchasesUpdated: Purchase Canceled")
        } else {
            Log.i("OTHERS_EXCEPTIONS", "onPurchasesUpdated: Error")
        }
    }

    override fun onStart() {
        super.onStart()
        super.onStart()
        //revisamos que elementos se han comprado al cargar la aplicación

        checkPurchases()
        //preparamos la conexión con los listeners
        //Por alguna razón en  onCreate no carga la primera vez al iniciar, probablemente sea por que es un método asíncrono y tiene cierta latencia
        //lo cargamos aquí para que no nos dé error de desconexión luego al realizar la compra
         establishConnection()
    }

    override fun onResume() {
        super.onResume()
        //Comprobamos que el producto haya sido comprado correctamente(Purchase.PurchaseState.PURCHASED)
        //para realizar la acción post-compra que necesitemos

        billingClient!!.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                list.forEach {purchase->
                    //comprobamos que el elemento seleccionado se haya comprado realmente
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        //Pasamos a verificarlo
                        verifyPurchase(purchase)
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        billingClient?.endConnection()
        super.onDestroy()
    }
}