## Dobiss

Dobiss-NXT has built in support for SMA and Solaredge inverters to query energy production directly from the PV converter.
Unfortunately EnvoyS is not supported, but no panic, as EnvoyS has an API to query statistics its always possible to
hook it up through some proxy service.

In comes the `Dobiss-Modbus-TCP-Proxy`.

What this project does is mimic an SMA converter towards Dobiss-NXT so that PV production/consumption stats
can be correctly visualized in the Dobiss-NXT UI, coming from an EnvoyS converter.

![](assets/dobiss-config.png)
Simply configure the IP address and listening port of the `Dobiss-Modbus-TCP-Proxy` into the Dobiss-NXT _Energy Configuration_
panel and you're ready to go.

### Modbus-TCP SMA

The table below gives an overview of the Modbus registers that are being read by Dobiss-NXT

| address | description                                      | size | type | dec. |R/W |
|---------|--------------------------------------------------|------|------|------|----|
| 30775   | Current active power on all line conductors (W)  | 2    | S32  | FIX0 | RO |
| 30529   | Total yield (Wh) [E-Total]                       | 2    | U32  | FIX0 | RO |

## EnvoyS

The idea is thus to map the registers depicted above to the correct stats coming from EnvoyS.
EnvoyS has a bunch of APIs to query stuff, but the one that gives us the most useful data is `/ivp/meters/readings`.

### /ivp/meters

The `/ivp/meters` endpoint can be used to get the `eid` for the production statistics queried through `/ivp/meters/readings`.
It's the `eid` of the `production` measurement type that needs to be used to filter on the `/ivp/meters/readings` data to get
the proper production stats Dobiss-NXT expects.

``` json
[
    {
        "eid": 123456789,
        "measurementType": "production",
    }
]
```

### /ivp/meters/readings

With the `eid` for production, the `/ivp/meters/readings` endpoint from EnvoyS is called to fetch all meter readings.
They are then filtered on `eid == (the eid for production)`.
The only readings of interest are `actEnergyDlvd` and `activePower` as they perfectly contain the data that Dobiss-NXT expects.

``` json
[
    {
        "eid"           : 123456789,
        "actEnergyDlvd" : 999999.999           // total yield (Wh) [lifetime]
        "activePower"   : 999.999,             // current production (W)
        "channels"      : [...]
    }
]
```

#### actEnergyDlvd
The **total production** of the PV installation up till now in Wh.
Dobiss-NXT uses this to populate the historical graphs in the UI.

#### activePower
The **current production** of the PV installation in Watts.  
Used in the Dobiss-NXT UI to visualize the current status and previous 24 hour graphs (the green values/bars in the graphs below).

|                                         |                                     |
| --------------------------------------- | ----------------------------------- |
|![](assets/dobiss-energy-ui-current.png) | ![](assets/dobiss-energy-ui-24h.png)|

# Dobiss-Modbus-TCP-Proxy

The project is written in Scala (2.13) and can easily be containerized to run on a NAS, Raspberry-PI, or any other platform.

## Requirements

|            |                                                                 |
|------------|-----------------------------------------------------------------|
| **memory** | 100 MiB                                                         |
| **CPU**    | 1 core is sufficient <br> it hardly uses any compute resources. |
| **disk**   | _not used_                                                      |
| **network**| approximately 5K per minute, so should be hardly noticable      | 

## Build

### Compile and Test
```
sbt clean compile coverage test coverageReport
```
Coverage HTML file is generated in `./target/scala-2.13/scoverage-report/index.html`

### Create container
```
sbt clean docker:publishLocal
```

### Create assembly
```
sbt clean assembly
```
The Uber-JAR is created in `./target/scala-2.13/DobissModbusProxy-assembly-<version>.jar`

To run the application from this JAR file: `java -jar DobissModbusProxy-assembly-<version>.jar`

## Configuration
```
modbus {
  tcp.port  = 1502  // listening port of the proxy service
                    // port as used in the Dobiss-NXT configuration
}

poll.interval = "1 minute"

plugins = {
    EnvoyS = ${envoy}
}

envoy {
      class = cloutrix.energy.envoy.EnvoyDataProvider
      config {
          host = x.x.x.x        // IP address of EnvoyS
          port = 80             // HTTP listening port of EnvoyS
      }
}
```

Following environment variables can be used to configure the application:

| env                    | default | description                         |
|------------------------|---------|-------------------------------------|
| `MODBUS_TCP_PORT`      | 1502    | listening port of the proxy service |
| `ENVOY_HOST`           | -       | IP address of EnvoyS                |
| `ENVOY_PORT`           | 80      | API listening port of EnvoyS        |
