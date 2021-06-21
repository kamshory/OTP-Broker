# Feature

## Multiple Device

OTP Broker dapat menggunakan beberapa modem sekaligus. Pengiriman SMS akan menggunakan algoritma Round-Robin di mana semua modem yang aktif akan digunakan secara bergilir.

## USSD Support

OTP Broker memungkinkan pengguna melakukan perintah USSD pada masing-masing modem. Perintah USSD tentu saja tergantung dari masing-masing operator seluler yang digunakan pada masing-masing SIM card yang terpasang pada masing-masing modem.

## Manual SMS

Manual SMS digunakan untuk menguji apakah masing-masing modem dapat mengirim SMS.

## Administrator Setting

Administrator Setting adalah menu untuk melakukan konfigurasi administrator. Perangkat OTP Broker baru belum memiliki administrator. Pengguna harus memuat administrator terlebih dahulu sebelum menggunakannya. Silakan masuk ke akses poin OTP Broker sesuai dengan SSID dan password yang tertera pada brosur dan pindai QR Code pada brosur menggunakan smartphone.

Alamat bawaan dari web manajemen adalah http://192.168.0.11:8888 

**Username**
Username adalah pengenal administrator saat login ke OTP Broker

**Password**
Username adalah pengaman administrator saat login ke OTP Broker

**Phone Number**
Phone number dapat digunakan jika administrator lupa password. Password akan dikirim melalui SMS. Tentu saja ini baru bisa dilakukan ketika OTP Broker telah terkonfigurasi dengan benar.

**Email**
Email dapat digunakan jika administrator lupa password. Password akan dikirim melalui email. Tentu saja ini baru bisa dilakukan ketika OTP Broker telah terkonfigurasi dengan benar.

## API Setting

API Setting adalah konfigurasi REST API untuk mengirimkan SMS.
1. **HTTP Port** adalah port server untuk HTTP
2. **Enable HTTP** adalah pengaturan untuk mengaktifkan atau menonaktifkan port HTTP
3. **HTTPS Port** adalah port server untuk HTTPS
4. **Enable HTTPS** adalah pengaturan untuk mengaktifkan atau menonaktifkan port HTTPS
5. **Message Path** adalah path untuk mengirimkan SMS dan email
6. **Blocking Path** adalah path untuk memblokir nomor telepon agar OTP Broker tidak mengirimkan SMS ke nomor tersebut
7. **Unblocking Path** adalah path untuk membuka blokir nomor telepon agar OTP Broker dapat kembali mengirimkan SMS ke nomor tersebut

## API User

API User adalah akun pengirim SMS melalui REST API.

**Username**
Username adalah pengenal pengirim saat mengirimkan SMS ke OTP Broker

**Password**
Username adalah pengaman pengirim saat mengirimkan SMS ke OTP Broker

**Phone Number**
Phone number adalah informasi kontak berupa nomor telepon dari pengguna API

**Email**
Email adalah informasi kontak berupa alamat email dari pengguna API

## Feeder Setting

OTP Broker memberikan pilihan apabila perangkat ini dipasang pada jaringan internet mobile atau pada jaringan di mana perangkat pengirim tidak mungkin dapat menjangkau alamat dari OTP Broker.

OTP Broker menyediakan 2 cara agar OTP Broker dapat menerima pesan yang akan dikirimkan melalui SMS yaitu dengan RabbitMQ dan WSMessageBroker.

## SMS Setting

SMS Setting adalah konfigurasi pengiriman SMS oleh OTP Broker.

## Modem

Modem adalah daftar modem yang terpasang pada OTP Broker. Modem diberi nama berdasarkan merek dan model perangkat serta koneksi yang digunakan. Modem dapat diaktifkan dan dinonaktirkan kapan saja. Modem yang tidak aktif tidak akan digunakan untuk mengirimkan SMS meskipun secara fisik terpasang pada OTP Broker dan menerima aliran daya.


## Email Setting

Email Setting adalah konfigurasi SMTP untuk mengirimkan password apabila pengguna lupa. Reset password dapat menggunakan username maupun alamat email.

## DDNS Record

DDNS Record adalah data untuk melakukan pengaturan DNS secara dinamis. DDNS atau Dymanic Domain Name Server adalah sebuah mekanisme pengaturan DNS yang dilakukan secara berulang-ulang disebabkan karena alamat IP publik dari server yang selalu berubah-ubah.

OTP Broker menyediakan pengaturan DDNS menggunakan Cloudflare dan NoIP. Pada Cloudflare, alamat IP publik dilakukan dengan mengirimkan request ke ipv4.icanhazip.com. DMS kemdian dikonfigurasi dengan alamat IP yang didapat. Pada NoIP, OTP Broker hanya akan mengirimkan nama server yang akan diatur ulang alamat IPnya. NoIP akan menggunakan alamat IP publik dari mana request tersebut berasal.

## Network Setting 

Network Setting adalah konfigurasi untuk mengatur jaringan dari OTP Broker. OTP Broker dilengkapi dengan akses poin sehingga dapat diakses langsung menggunakan laptop atau handphone tanpa harus memasangnya ke jaringan kabel. Hal ini akan memudahkan pengguna karena konfigurasi jaringan LAN pengguna berbeda-beda. Pengguna cukup mengatur alamat IP pada jaringan ethernet sesuai dengan konfigurasi jaringan LAN yang digunakan.

### DHCP

Konfigurasi DHCP akan mengatur DHCP pada akses poin OTP Broker.

### Wireless LAN

Konfigurasi Wireless LAN akan mengatur alamat IP pada jaringan wireless OTP Broker. Alamat IP bawaan dari OTP Broker adalah 192.168.0.11

### Ethernet

Konfigurasi Ethernet akan mengatur alamat IP ethernet pada OTP Broker.

## Cloudflare

Modul Cloudflare adalah modul untuk mengatur akun Cloudflare yang digunakan.

## NoIP

Modul NoIP adalah modul untuk mengatur akun NoIP yang digunakan.


## Pengujian Modul

**Home** 

1. Service Status : OK
2. Server Status : OK

**USSD**

1. Execute USSD : OK

**SMS**

1. Send SMS : OK

**Administrator**

1. View : OK
2. Add : OK
3. Edit : OK
4. Delete : OK
5. Activate : OK
6. Deactivate : OK
7. Block : OK
8. Unblock : OK
	
**API Setting**

1. Update : OK
2. API User
3. View : OK
4. Add : OK
5. Edit : OK
6. Delete : OK
7. Activate : OK
8. Deactivate : OK
9. Block : OK
10. Unblock : OK
	
**Feeder Setting**

1. WSMessageBroker : OK
2. RabbitMQ : OK
	
**SMS Setting**

1. Send SMS : OK

**Modem**

1. Add : OK
2. Edit : OK
3. Delete : OK
4. Activate : OK
5. Deactivate : OK
	
**Email Setting**

1. Update

**DNS Record**

1. Add : OK
2. Edit : OK
3. Delete : OK
4. Activate : OK
5. Deactivate : OK
6. Proxied : OK
7. Unproxied : OK
	
**Network Setting**

1. DHCP : OK
2. Wireless LAN : OK
3. Ethernet : OK
	
**Cloudflare** 

1. Update : OK

**No IP**

1. Update : OK

