# kidney

A microservice framework designed to use different protocols and transport methods

## Highlights

- Transport agnostic (UDP, TCP, HTTP)
- Simple protocol (JSON, EDN, Transit)
- Discovery via Zookeeper and etcd

## Status

Protocols
- [x] JSON
- [ ] edn
- [ ] transit
- [ ] ProtocolBuffer

Transports
- [x] UDP
- [ ] HTTP
- [ ] TCP

Discover
- [ ] Zookeeper
- [ ] etcd

Client
- [x] Multiply server
- [x] async
- [ ] Circuit Breaker via Hysterix
- [x] Remote Errors
- [x] Timeout

Server
- [ ] Registration
- [x] Simple interface
- [ ] multiple language interface

Misc
- [x] travis CI
- [ ] examples
- [ ] submodels

## License

Copyright © 2016 Kai Strempel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
