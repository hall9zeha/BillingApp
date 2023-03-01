# BillingApp
<img src="https://github.com/hall9zeha/BillingApp/blob/main/InJava/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" align="left"
width="20%" hspace="10" vspace="10">
</br>
</br>
Aplicación Android para probar la librería de compras integradas de Google
</br>
</br>
</br>
</br>
</br>
</br>

# Requisitos muy importantes

Para poder probar la funcionalidad del código de este repositorio se debe cumplir lo siguiente:

* Tener una cuenta de desarrollador en Google (Play console)
* Crear una aplicación en Play console (Como borrador o usar una que esté en producción) y subir la app en pruebas internas.(Esto podría tomar unas horas en estar 
  disponible para realizar nuestras pruebas)
  - Importante: la aplicación que subamos a pruebas internas para probar la librería de compras debe ser la misma  que la de producción,  o bien si la subimos por 
    primera vez en pruebas internas sin enviarla a produccción, no cambiar el nombre del paquete, en ambos casos el app-bundle debe estar firmado. 
  - El código de este repositorio se probó correctamente en pruebas internas de una aplicación que ya estaba en producción. Puede que en modo borrador google siga 
    permitiendo la prueba de su libreria de compras, si no es así probar con una app en modo de producción.

* Luego añadir testers (entre los cuales podemos incluirnos también) en play Console en las configuraciones de la app para nuestras pruebas internas, solamente 
  correos de google.
  Enviar el enlace de la app a los testers que hayamos elegido.
* Habilitar "Licencia para testing" en el panel global de play console, muy importante para probar la librería de compras integradas,(Las licencias deben estar en modo LICENCED) de lo contrario no funcionará.
* Los testers deberán aceptar la invitación del enlace enviado para poder descargar la app de pruebas internas y ralizarlas. 
* Ahora una vez terminados todos los pasos podemos probar nuestra app en modo debug desde android studio (Como así lo hizo el autor de este repositorio)
  y ver los eventos que se desencadenan internamente. Importante el dispositivo que se usó para probar en modo debug contenía una cuenta de tester (puede ser que por 
  eso las pruebas desde android studio funcionaron), independientemente de todo esto las pruebas tanto en emuladores como en dispositivos físicos de los tester, 
  funcionaron correctamente.

## Bonus

Para reiniciar los productos comprados en las pruebas, reembolar las compras hechas desde play console y estarán listas para probar nuevamente, en nuestros testers o emuladores.
