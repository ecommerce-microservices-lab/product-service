# Product Api

prefijo `/product-service`

Obtener todos los productos

GET `/api/products`

Funciona bien

Obtener producto por id

GET `/api/products/{productId}`

Crear producto

POST `/api/products`

Se puede pasar id entonces no crea sino que sobreescribe, se puede crear sin datos practicamente, No se maneja bien la excepcion de que no exista la categoria


Editar producto por body

PUT `/api/products`

Lo actualiza si existe pero si no existe lo crea


# Category API

prefijo `/product-service`

Obtener todos los productos

GET `/api/categories`

Funciona bien

Obtener categoria por id

GET `/api/categories/{categoryId}`

Funciona bien

ci: trigger develop Wed Oct 29 11:04:09 -05 2025
