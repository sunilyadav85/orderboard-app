I have written comments throughout the application explaining the decision I made while writing this app.

This application can be launched with Swagger.
Please run the class OrderBoardApplication using debug/run configuration in IDE and then go to Swagger link below
Swagger Link : http://localhost:55525/swagger-ui.html

Alternatively, you can create a jar file using "mvn clean package" and run the app using "java -jar orderboard-app-1.0.0.jar" at the command line
Use postman or any other REST client to access the Api
GET http://localhost:55525/order
DELETE http://localhost:55525/order/{orderId}
POST http://localhost:55525/order
Sample Request Body for post -> {"user":"Test User","quantity":8.0,"price":10,"orderType":"BUY"}