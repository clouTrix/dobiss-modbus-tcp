package envoy

object Envoy {

  def sampleMeterReadings(activePower: Double, actEnergyDlvd: Double): String =
   s"""
      |[{
      |  "eid": 666666666,
      |  "timestamp": 1675348155,
      |  "actEnergyDlvd": ${actEnergyDlvd},
      |  "actEnergyRcvd": 413195.96,
      |  "apparentEnergy": 16999522.093,
      |  "reactEnergyLagg": 5658482.464,
      |  "reactEnergyLead": 1987438.886,
      |  "instantaneousDemand": 455.202,
      |  "activePower": ${activePower},
      |  "apparentPower": 719.558,
      |  "reactivePower": 478.046,
      |  "pwrFactor": 0.636,
      |  "voltage": 706.284,
      |  "current": 3.057,
      |  "freq": 50,
      |  "channels": [
      |    {
      |      "eid": 1778385169,
      |      "timestamp": 1675348155,
      |      "actEnergyDlvd": 3088063.238,
      |      "actEnergyRcvd": 15188.956,
      |      "apparentEnergy": 3772470.003,
      |      "reactEnergyLagg": 1438121.485,
      |      "reactEnergyLead": 0.01,
      |      "instantaneousDemand": 91.288,
      |      "activePower": 91.288,
      |      "apparentPower": 130.141,
      |      "reactivePower": 100.505,
      |      "pwrFactor": 0.667,
      |      "voltage": 235.653,
      |      "current": 0.553,
      |      "freq": 50
      |    },
      |    {
      |      "eid": 1778385170,
      |      "timestamp": 1675348155,
      |      "actEnergyDlvd": 3008389.542,
      |      "actEnergyRcvd": 384594.812,
      |      "apparentEnergy": 5349178.746,
      |      "reactEnergyLagg": 2389481.928,
      |      "reactEnergyLead": 1611.028,
      |      "instantaneousDemand": 163.292,
      |      "activePower": 163.292,
      |      "apparentPower": 264.907,
      |      "reactivePower": 164.876,
      |      "pwrFactor": 0.633,
      |      "voltage": 235.328,
      |      "current": 1.126,
      |      "freq": 50
      |    },
      |    {
      |      "eid": 1778385171,
      |      "timestamp": 1675348155,
      |      "actEnergyDlvd": 5762320.068,
      |      "actEnergyRcvd": 13412.192,
      |      "apparentEnergy": 7877873.344,
      |      "reactEnergyLagg": 1830879.052,
      |      "reactEnergyLead": 1985827.848,
      |      "instantaneousDemand": 200.622,
      |      "activePower": 200.622,
      |      "apparentPower": 324.51,
      |      "reactivePower": 212.665,
      |      "pwrFactor": 0.627,
      |      "voltage": 235.303,
      |      "current": 1.378,
      |      "freq": 50
      |    }
      |  ]
      |},
      |{
      |  "eid": 704643584,
      |  "timestamp": 1675348155,
      |  "actEnergyDlvd": 3720026.3,
      |  "actEnergyRcvd": 5379322.057,
      |  "apparentEnergy": 12853301.037,
      |  "reactEnergyLagg": 107587.004,
      |  "reactEnergyLead": 5874723.85,
      |  "instantaneousDemand": 1320.193,
      |  "activePower": 1320.193,
      |  "apparentPower": 1598.532,
      |  "reactivePower": -534.075,
      |  "pwrFactor": 0.826,
      |  "voltage": 706.217,
      |  "current": 6.791,
      |  "freq": 50,
      |  "channels": [
      |    {
      |      "eid": 1778385425,
      |      "timestamp": 1675348155,
      |      "actEnergyDlvd": 1026049.472,
      |      "actEnergyRcvd": 1061501.264,
      |      "apparentEnergy": 3028700.157,
      |      "reactEnergyLagg": 77764.026,
      |      "reactEnergyLead": 1297180.119,
      |      "instantaneousDemand": 417.205,
      |      "activePower": 417.205,
      |      "apparentPower": 468.565,
      |      "reactivePower": -59.967,
      |      "pwrFactor": 0.895,
      |      "voltage": 235.755,
      |      "current": 1.987,
      |      "freq": 50
      |    },
      |    {
      |      "eid": 1778385426,
      |      "timestamp": 1675348155,
      |      "actEnergyDlvd": 1054252.062,
      |      "actEnergyRcvd": 2021873.506,
      |      "apparentEnergy": 4581441.281,
      |      "reactEnergyLagg": 15574.33,
      |      "reactEnergyLead": 2277570.373,
      |      "instantaneousDemand": 407.605,
      |      "activePower": 407.605,
      |      "apparentPower": 503.373,
      |      "reactivePower": -211.009,
      |      "pwrFactor": 0.804,
      |      "voltage": 235.288,
      |      "current": 2.14,
      |      "freq": 50
      |    },
      |    {
      |      "eid": 1778385427,
      |      "timestamp": 1675348155,
      |      "actEnergyDlvd": 1639724.767,
      |      "actEnergyRcvd": 2295947.288,
      |      "apparentEnergy": 5243159.598,
      |      "reactEnergyLagg": 14248.647,
      |      "reactEnergyLead": 2299973.359,
      |      "instantaneousDemand": 495.383,
      |      "activePower": 495.383,
      |      "apparentPower": 626.595,
      |      "reactivePower": -263.099,
      |      "pwrFactor": 0.791,
      |      "voltage": 235.174,
      |      "current": 2.664,
      |      "freq": 50
      |    }
      |  ]
      |}]
      |""".stripMargin

  def sampleMeter: String =
    """
      |[{
      |      "eid": 666666666,
      |      "state": "enabled",
      |      "measurementType": "production",
      |      "phaseMode": "three",
      |      "phaseCount": 3,
      |      "meteringStatus": "normal",
      |      "statusFlags": []
      | },
      | {
      |      "eid": 704643584,
      |      "state": "enabled",
      |      "measurementType": "net-consumption",
      |      "phaseMode": "three",
      |      "phaseCount": 3,
      |      "meteringStatus": "normal",
      |      "statusFlags": []
      | }]
      |""".stripMargin
}
