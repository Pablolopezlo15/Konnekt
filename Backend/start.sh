#!/bin/bash

mkdir -p /app/certs

if [ ! -f /app/certs/key.pem ]; then
  echo "🔑 Generando claves SSL..."
  openssl genrsa -out /app/certs/key.pem 2048
  openssl req -new -x509 -key /app/certs/key.pem -out /app/certs/cert.pem -days 365 -subj "/CN=localhost"
fi

exec uvicorn main:app --host 0.0.0.0 --port 443 \
  --ssl-keyfile /app/certs/key.pem \
  --ssl-certfile /app/certs/cert.pem
