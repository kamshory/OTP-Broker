# OTP Broker

OTP Broker adalah server untuk mengirimkan SMS melalui protokol HTTP, WebSocket dan Message Broker. Pengguna dapat memasang OTP Broker pada server dengan IP address statis yang diapat diakses oleh klien yang akan mengirimkan SMS. Selain itu, pengguna juga dapat memasang OTP Broker pada server dengan IP address dinamis. Server ini kemudian mengakses sebuah server websocket atau server RabbitMQ. OTP Broker bertindak sebagai consumer yang akan mengirimkan semua SMS yang diterimanya.

Baik WebSocket maupun Message Broker menggunakan sebuah channel yang dapat diseting dari kedua sisi (pengirim dan penerima).

Untuk menggunakan WebSocket, silakan gunakan library WSMessageBrocker dengan link https://github.com/kamshory/WSMessageBrocker atau anda dapat membuatnya sendiri. 

Untuk menggunakan Message Broker, silakan gunakan RabbitMQ dengan link https://www.rabbitmq.com/

![OTP Broker Topology](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology.png)

