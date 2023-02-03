package saj

object SAJ {

  def sampleMeterReadings(activePower: Double, actEnergyDlvd: Double): String =
   s"""
      |<real_time_data>
      |  <state>Normal</state>
      |  <Vac_l1>238.6</Vac_l1>
      |  <Vac_l2>240.2</Vac_l2>
      |  <Vac_l3>238.0</Vac_l3>
      |  <Iac_l1>0.49</Iac_l1>
      |  <Iac_l2>0.50</Iac_l2>
      |  <Iac_l3>0.51</Iac_l3>
      |  <Freq1>49.95</Freq1>
      |  <Freq2>50.00</Freq2>
      |  <Freq3>49.99</Freq3>
      |  <pac1>97</pac1>
      |  <pac2>97</pac2>
      |  <pac3>106</pac3>
      |  <p-ac>${activePower}</p-ac>
      |  <temp>31.0</temp>
      |  <e-today>0.59</e-today>
      |  <t-today>3.3</t-today>
      |  <e-total>${actEnergyDlvd}</e-total>
      |  <CO2>50844.65</CO2>
      |  <t-total>29430.7</t-total>
      |  <v-pv1>328.9</v-pv1>
      |  <i-pv1>0.58</i-pv1>
      |  <v-pv2>311.7</v-pv2>
      |  <i-pv2>0.67</i-pv2>
      |  <v-bus>630.2</v-bus>
      |  <maxPower>472</maxPower>
      |</real_time_data>
      |""".stripMargin
}
