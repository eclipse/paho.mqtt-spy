#!/bin/bash

openssl genrsa -out client.key 2048
openssl req -new -out client.csr \
  -key client.key -subj "/CN=client/O=example.com"
openssl x509 -req -in client.csr -CA ca.crt \
  -CAkey ca.key -CAserial ca.srl -out client.crt \
  -days 3650 -addtrust clientAuth
