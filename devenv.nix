{ pkgs, lib, config, inputs, ... }:

{
  packages = [
    pkgs.git
    pkgs.coreutils
    pkgs.mill
    pkgs.nodejs
    pkgs.jdk17
  ];

  services.postgres = {
    enable = true;
    package = pkgs.postgresql_16;

    initialDatabases = [{ name = "postgres"; }];

    listen_addresses = "localhost";
    port = 5432;

    initialScript = ''
      CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres' CREATEDB;
      CREATE DATABASE postgres;
      GRANT ALL PRIVILEGES ON DATABASE postgres TO postgres;
    '';
  };

  processes = {
    backend.exec = "mill -i -j 0 -w tasklist.backend.runBackground";
    frontend.exec = "mill -i -j 0 -w tasklist.frontend_vite.compile";
    vite.exec = "cd tasklist/frontend_vite && npm install && npm run dev";
  };
}
