package com.barryzea.billingapp.views;

import static com.barryzea.billingapp.common.Constants.PREMIUM_ID;
import static com.barryzea.billingapp.common.Constants.REMOVE_ADS_ID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.barryzea.billingapp.MyApp;
import com.barryzea.billingapp.R;
import com.barryzea.billingapp.databinding.ActivityMainBinding;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private ActivityMainBinding bind;
    private BillingClient billingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        doMyBiller();
        setUpListeners();
        setMessageStatusOfPurchase();

    }
    private void setMessageStatusOfPurchase(){
        //Llenamos el textView correspondiente con un mensaje si hay productos comprados o no
        //o podemos realizar cualquier evento relacionado con la compra de los productos dentro de la app
        //tales como desactivar los anuncios o habilitar funciones para usuario premium
        //así como dejar de llamar a los productos.
        if(MyApp.prefs.getBuyRemoveAds() && MyApp.prefs.getBuyPremiumUser()){
            bind.tvHello.setText(R.string.allPaidMsg);
        }  else if(MyApp.prefs.getBuyRemoveAds()){
            bind.tvHello.setText(R.string.removeAdsPaid);
        }else if(MyApp.prefs.getBuyPremiumUser()){
            bind.tvHello.setText(R.string.premiumUserPaid);
        }

    }
    private void setUpListeners(){
        bind.btnPurchase.setOnClickListener(v->{
           //si no cargan los productos al inicio los llamamos en este botón
            showProducts();
        });
    }
    private void doMyBiller(){
        //inicializamos nuestro cliente de compras
        billingClient = BillingClient.newBuilder(MainActivity.this)
                .enablePendingPurchases()
                .setListener(this).build();
        //podríamos llamar aquí también a la función establishConnection() pero ya lo hacemos en onStart:
        // establishConnection()
    }
    private void establishConnection(){
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                //Si hubiera un error de desconexión volvemos a ejecutar la función
                establishConnection();
                Log.e("ON_DISCONECT", "desconectado" );
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK ){
                    //si la conexión es exitosa consultamos los productos de la app desde google play
                    //y los mostramos
                    if (!MyApp.prefs.getBuyRemoveAds()) {
                        showProducts();
                    }
                    Log.d("ON_FINISH", "Conectado" );
                }
            }
        });
    }

    private void showProducts(){
        //Primero creamos una lista con los mismos productos que tenemos creados en play console para nuestra app
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(REMOVE_ADS_ID)//REMOVE_ADS_ID viene de la clase Constants
                        .setProductType(BillingClient.ProductType.INAPP)
                        //.setProductType(BillingClient.ProductType.SUBS)//el en caso de tener suscripciones usar esta forma
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_ID)//PREMIUM_ID viene de la clase Constants
                        .setProductType(BillingClient.ProductType.INAPP)
                        //.setProductType(BillingClient.ProductType.SUBS)//el en caso de tener suscripciones usar esta forma
                        .build()
        );
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();
        //ahora escuchamos la respuesta y si nos trae una confirmación de productos existentes manejamos lo siguiente:
        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetails) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    if(!productDetails.isEmpty()){

                        for(ProductDetails detail : productDetails) {
                            //comparamos si el primer elemento de la lista traida desde nuestra cuenta de google
                            //es igual al id del producto
                            if(detail.getProductId().equals(REMOVE_ADS_ID)) {
                                //comprobamos si en nuestras preferencias guardadas la compra de este producto
                                //esta en falso para así mostrar las vistas del producto, caso contrario no se mostrará
                                if(!MyApp.prefs.getBuyRemoveAds()) {
                                    //List<ProductDetails.SubscriptionOfferDetails> subDetails = detail.getSubscriptionOfferDetails(); si nuestra lista tiene suscripciones usar esta forma  para acceder a los detalles
                                    runOnUiThread(() -> {

                                        //llenamos las vistas con la descripción y el precio de nuestro producto
                                        bind.purchaseCard.setVisibility(View.VISIBLE);
                                        bind.tvDescription.setText(detail.getDescription());

                                        //para obtener los detalles de un producto INAPP o de una sola compra
                                        //se hace con getOneTimePurchaseOfferDetails()
                                        bind.tvPrice.setText(detail.getOneTimePurchaseOfferDetails().getFormattedPrice());

                                        //para obtener los detalles de un producto SUBS o suscripción:
                                        //bind.tvPrice.setText(subDetails.get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice()); si fuera una suscripción usar esta forma

                                        bind.purchaseCard.setOnClickListener(v -> {
                                            //iniciamos el evento click de cualquier elemento que designemos para ello
                                            //enviándole los detalles del producto cargado para que se muestre el mensaje de compra
                                            launchPurchaseFlow(detail);
                                        });
                                        //o bien podriamos lanzar el evento de compra directamente launcPurchaseFlow(detail) si solo tuvieramos un producto
                                    });
                                }
                            }
                            //lo mismo si hubiera un segundo elemento en nuestra lista de productos
                            if(detail.getProductId().equals(PREMIUM_ID)) {
                                //comprobamos si en nuestras preferencias guardadas la compra de este producto
                                //esta en falso para así mostrar las vistas del producto, caso contrario no se mostrará
                                if (!MyApp.prefs.getBuyPremiumUser()) {
                                    runOnUiThread(() -> {
                                        bind.premiumCard.setVisibility(View.VISIBLE);
                                        bind.tvDescription1.setText(detail.getDescription());
                                        bind.tvPrice1.setText(detail.getOneTimePurchaseOfferDetails().getFormattedPrice());
                                        bind.premiumCard.setOnClickListener(v -> {
                                            launchPurchaseFlow(detail);
                                        });
                                    });
                                }
                            }
                        }
                      }
                    else{
                        Toast.makeText(MainActivity.this, "No se encontró el ítem de producto", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Error " +billingResult.getDebugMessage() , Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
    void launchPurchaseFlow(ProductDetails productDetails) {
        //en este caso al ser un producto de una sola compra y no suscripción usamos el método getOneTimePurchaseOfferDetails()
        assert productDetails.getOneTimePurchaseOfferDetails() != null;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                //Esta parte solo es necesaria para suscripciones
                                //.setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();
        //listo para lanzar el diálogo de compra de cada elemento cargado en nuestras vistas
        BillingResult billingResult = billingClient.launchBillingFlow(MainActivity.this, billingFlowParams);
    }
    private void verifyPurchase(Purchase purchases){
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //Ahora que la compra ha sido confirmada podemos  ejecutar el código que sea conveniente para nosotros
                //En este caso al ser una compra para quitar los anuncios de la app, se guardará un valor en preferencias
                //para desactivar los anuncios
                switch (purchases.getProducts().get(0)) {//con esta línea traemos el ID de nuestro producto (getSkus() esta descontinuado pero funciona)
                    //en este caso usaremos getProducts()

                    //Si tenemos más de un producto y queremos guardar en preferencias locales o en nuestro servidor
                    //cualquier dato que consideremos adecuado para nosotros, debemos de comparar el ID de nuestro producto
                    //con el que se ha comprado y realizar cualquier evento que tengamos planeado
                    case REMOVE_ADS_ID: { //en el caso de que se haya comprado: desactivar anuncios, hacer los siquiente:
                        MyApp.prefs.setBuyRemoveAds(true);
                        if (MyApp.prefs.getBuyRemoveAds()) {
                            bind.purchaseCard.setVisibility(View.GONE);
                            setMessageStatusOfPurchase();
                            Toast.makeText(this, R.string.successfulPurchase, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                    case PREMIUM_ID:{//en el caso de haber comprado usuario premium lo siguiente:
                        MyApp.prefs.setBuyPremiumUser(true);
                        if (MyApp.prefs.getBuyPremiumUser()) {
                            bind.premiumCard.setVisibility(View.GONE);
                            setMessageStatusOfPurchase();
                            Toast.makeText(this, R.string.successfulPurchase, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }

            }
        });
        Log.d("TAG", "Purchase Token: " + purchases.getPurchaseToken());
        Log.d("TAG", "Purchase Time: " + purchases.getPurchaseTime());
        Log.d("TAG", "Purchase OrderID: " + purchases.getOrderId());
    }


    void checkPurchases(){
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    //.setProductType(BillingClient.ProductType.SUBS) para suscripciones
                                    .build(), (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                    Log.d("TEST_PRODUCTS_SIZE",list.size()+ " size");
                                    if(list.size()>0){
                                        runOnUiThread(()->{
                                            Toast.makeText(MainActivity.this, "Compras verificadas", Toast.LENGTH_SHORT).show();
                                        });
                                        //si tenemos un sólo producto no tenemos que revisar la lista con un ciclo
                                        //MyApp.prefs.setBuyRemoveAds(true) guardamos el estado sencillamente aquí

                                        for (Purchase purchase: list){
                                            //si la devolución de productos comprados en este dispositivo tiene multiples suscripciones o varios productos de una sola compra
                                            //las revisamos aquí
                                            switch (purchase.getProducts().get(0)){
                                                case REMOVE_ADS_ID:
                                                    MyApp.prefs.setBuyRemoveAds(true);
                                                    break;
                                                case PREMIUM_ID:
                                                    MyApp.prefs.setBuyPremiumUser(true);
                                                    break;
                                            }

                                        }
                                    }else {
                                        MyApp.prefs.setBuyRemoveAds(false);
                                        MyApp.prefs.setBuyPremiumUser(false);

                                    }
                                }
                            });
                }
            }
        });
    }
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchaseList) {
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseList !=null){
            for(Purchase purchase:purchaseList){
                //si se ha presionado en comprar producto dentro del dialog de compras, verificamos
                verifyPurchase(purchase);
            }
        }
        else if (billingResult.getResponseCode() ==
                BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i("CANCELED", "onPurchasesUpdated: Purchase Canceled");
        } else {
            Log.i("OTHERS_EXCEPTIONS", "onPurchasesUpdated: Error");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //revisamos que elementos se han comprado al cargar la aplicación
        checkPurchases();
        //preparamos la conexión con los listeners
        //Por alguna razón en  onCreate no carga la primera vez al iniciar, probablemente sea por que es un método asíncrono y tiene cierta latencia
        //lo cargamos aquí para que no nos dé error de desconexión luego al realizar la compra
         establishConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Comprobamos que el producto haya sido comprado correctamente(Purchase.PurchaseState.PURCHASED)
        //para realizar la acción post-compra que necesitemos
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, list) -> {
                    if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                        for(Purchase purchase: list){
                    //comprobamos que el elemento seleccionado se haya comprado realmente
                            if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()){
                                //Pasamos a verificarlo
                                verifyPurchase(purchase);
                            }
                        }
                    }
                }
        );
    }
    @Override
    protected void onDestroy() {
        billingClient.endConnection();
        super.onDestroy();
    }
}