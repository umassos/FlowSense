/*
  BLE_Peripheral.ino

  This program uses the ArduinoBLE library to set-up an Arduino Nano 33 BLE 
  as a peripheral device and specifies a service and a characteristic. Depending 
  of the value of the specified characteristic, an on-board LED gets on. 

  The circuit:
  - Arduino Nano 33 BLE. 

  This example code is in the public domain.
*/

#include <ArduinoBLE.h>

const char* deviceServiceUuid = "7e889611-7c22-4017-96fc-5a2d23e4bcb3";
const char* deviceServiceCharacteristicUuid = "e4181542-6f68-461a-811a-e71f604bfa4f";
const int OutPin  = A0;   // wind sensor analog pin  hooked up to Wind P sensor "OUT" pin

char input;

BLEService airflowService(deviceServiceUuid); 
BLECharacteristic airflowCharacteristic(deviceServiceCharacteristicUuid, BLENotify | BLERead, 8, true);

void setup() {
  Serial.begin(9600);
  //while (!Serial);  
  
  pinMode(LEDR, OUTPUT);
  pinMode(LEDG, OUTPUT);
  pinMode(LEDB, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  
  digitalWrite(LEDR, HIGH);
  digitalWrite(LEDG, HIGH);
  digitalWrite(LEDB, HIGH);
  digitalWrite(LED_BUILTIN, LOW);

  
  if (!BLE.begin()) {
    Serial.println("- Starting BLE module failed!");
    while (1);
  }
  
  BLE.setLocalName("Arduino Nano 33 (Peripheral)");
  BLE.setAdvertisedService(airflowService);
  airflowService.addCharacteristic(airflowCharacteristic);
  BLE.addService(airflowService);
  BLE.advertise();

  Serial.println("Nano 33 BLE (Peripheral Device)");
  Serial.println(" ");
}

void loop() {
  BLEDevice central = BLE.central();
  char strValue[8];
  Serial.println("- Discovering central device...");
  delay(500);

  if (central) {
    Serial.println("* Connected to central device!");
    Serial.print("* Device MAC address: ");
    Serial.println(central.address());
    Serial.println(" ");

    while (central.connected()) {
      int windADunits = analogRead(OutPin);
      // wind formula derived from a wind tunnel data, annemometer and some fancy Excel regressions
      // this scalin doesn't have any temperature correction in it yet
      float windMPH =  pow((((float)windADunits - 252.0) / 85.6814), 3.36814);
      Serial.println(String(windMPH*0.44704, 2));
      
      String medString = String(windMPH*0.44704, 2);
      int size = medString.length();
      medString.toCharArray(strValue, size+1);
      
      airflowCharacteristic.setValue((unsigned char*)strValue, 8);
      Serial.println((char *)airflowCharacteristic.value());
      
      delay(100);
    }
    
    Serial.println("* Disconnected from central device!");
  }
}
