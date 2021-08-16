# IntelliJ Pairing Plugin

[![Build Status](https://travis-ci.org/tyro/pairing-tool.svg?branch=master)](https://travis-ci.org/tyro/pairing-tool)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This plugin has been designed to make pairing more natural for developers using different machines.

## Installation

Installation is now super simple. Just search the Jetbrains store for `pairing tool`

![intellij plugin installation](img/intellij_plugin_installation.png)

Note: If you have installed this plugin manually, you will have to uninstall it and reinstall it from the store to allow the jetbrains store to manage the versions for you. 

For un-released code use:
```bash
./build-plugin.sh
```

## Run a kafka image somewhere
```bash
./run-kafka.sh
```

## Or Run a pairing-tool-websocket-server image somewhere

See README instructions [here](https://github.com/tyro/pairing-tool-websocket-server)

## Connect to a workspace

![PairingPanel](img/intellij_plugin_installation_kafka.png)

![PairingPanel](img/intellij_plugin_installation_web_socket.png)

To connect to a session just navigate to the Pairing Tool panel.
1) Choose the server type: (Kafka | WebSocket)
2) Put in a Host Url: <kafka.example.com> | http://example-server:4001
3) Give your session a name: \<exampleSession>
4) Tell your pair to use the same settings

Note: Use a unique session name so that you don't push updates to others who don't want them\
Note: For the websocket URL. Please use a valid URL including scheme (http), domain name and port

## Copyright and Licensing

Copyright 2020 Tyro Payments Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.
