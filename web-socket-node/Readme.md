# Pairing Tool Node Server

This server has been created as an alternative to the kafka server backend. This tool is intended for smaller teams who wish to use the tool who do not want the complexity of a kafka server.

## How to Run

Build the image
```bash
docker build . -t pairing-tool-server:node-alpha-1
```

Using default port
```bash
docker run -it -p 4001:4001 pairing-tool-server:node-alpha-1
```

Using a different the port
```bash
docker run -it -e PORT=4002 -p 4002:4002 pairing-tool-server:node-alpha-1
```
