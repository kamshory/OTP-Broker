# OTP Broker

OTP Broker adalah server untuk mengirimkan SMS melalui protokol HTTP, WebSocket dan Message Broker. Pengguna dapat memasang OTP Broker pada server dengan IP address statis yang diapat diakses oleh klien yang akan mengirimkan SMS. Selain itu, pengguna juga dapat memasang OTP Broker pada server dengan IP address dinamis. Server ini kemudian mengakses sebuah server websocket atau server RabbitMQ. OTP Broker bertindak sebagai consumer yang akan mengirimkan semua SMS yang diterimanya.

Baik WebSocket maupun Message Broker menggunakan sebuah channel yang dapat diseting dari kedua sisi (pengirim dan penerima).

Untuk menggunakan WebSocket, silakan gunakan library WSMessageBrocker dengan link https://github.com/kamshory/WSMessageBrocker atau anda dapat membuatnya sendiri. 

Untuk menggunakan Message Broker, silakan gunakan RabbitMQ dengan link https://www.rabbitmq.com/

![OTP Broker Topology](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology.png)

Sekenario 1 - OTP Broker Dapat Diakses App Server

Pada skenario ini, App Server dapat langsung mengirimkan OTP ke OTP Broker melalui HTTP.

![OTP Broker Topology Skenario 1](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology-1.png)

Sekenario 2 - OTP Broker Tidak Dapat Diakses App Server

Pada skenario ini, App Server dapat mengirimkan OTP ke RabbitMQ Server atau WSMessageBroker. WSMessageBroker menggunakan protokol WebSoket dan Basic Authentication. Baik App Server maupun OTP Broker bertindak sebagai client dari WSMessageBroker.

App Server bertindak sebagai publisher dan OTP Broker menjadi consumer dari RabbitMQ Server dan WSMessageBroker. Keduanya harus menggunakan channel yang sama agar semua OTP yang dikirimkan oleh App Server dapat diterima oleh OTP Broker.

![OTP Broker Topology Skenario 2](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology-2.png)

Dari kedua skenario di atas, OTP Broker akan mengirmkan SMS menggunakan modem GSM yang terpasang secara fisik pada perangkat OTP Broker.
