#!/usr/bin/env bash
set -euo pipefail

if [ "${EMBEDDED_DB:-0}" = "1" ]; then
  mkdir -p /run/mysqld /var/lib/mysql
  chown -R mysql:mysql /run/mysqld /var/lib/mysql
  if [ ! -d /var/lib/mysql/mysql ]; then
    mariadb-install-db --user=mysql --datadir=/var/lib/mysql >/dev/null 2>&1
  fi
  mariadbd --user=mysql --datadir=/var/lib/mysql --bind-address=127.0.0.1 \
    --socket=/run/mysqld/mysqld.sock --pid-file=/run/mysqld/mysqld.pid \
    --innodb-buffer-pool-size=32M --performance-schema=OFF --skip-log-bin &
  for i in $(seq 1 60); do
    if mysqladmin ping --silent >/dev/null 2>&1; then break; fi
    sleep 1
  done
  mysql -uroot <<SQL
CREATE DATABASE IF NOT EXISTS tunan_cinema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON tunan_cinema.* TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;
SQL
fi

exec java -Xms64m -Xmx220m -XX:MaxMetaspaceSize=96m -jar /app/app.jar
