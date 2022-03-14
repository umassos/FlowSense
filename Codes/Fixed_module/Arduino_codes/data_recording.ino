
// HTS221 sensor library
// https://www.arduino.cc/en/Reference/ArduinoHTS221
// https://github.com/arduino-libraries/Arduino_HTS221
#include <Arduino_HTS221.h>
// LPS22HB sensor library
// https://www.arduino.cc/en/Reference/ArduinoLPS22HB
// https://github.com/arduino-libraries/Arduino_LPS22HB
#include <Arduino_LPS22HB.h>
// LSM9DS1 sensor library
// https://www.arduino.cc/en/Reference/ArduinoLSM9DS1
// https://github.com/arduino-libraries/Arduino_LSM9DS1
#include <Arduino_LSM9DS1.h>
// APDS9960 sensor library
// https://www.arduino.cc/en/Reference/ArduinoAPDS9960
// https://github.com/arduino-libraries/Arduino_APDS9960
#include <Arduino_APDS9960.h>
// MP34DT05 sensor library
// https://www.arduino.cc/en/Reference/PDM
// https://github.com/arduino/ArduinoCore-nRF528x-mbedos/tree/master/libraries/PDM
#include <PDM.h>
// TBD
#include <arduinoFFT.h>
#include <TimeLib.h>
#include <RTClib.h>

#include <SPI.h>
#include <SD.h>

unsigned long clocktime;
File myFile;
float clock_unix;

// change this to match your SD shield or module;
const int chipSelect = 10;


arduinoFFT FFT = arduinoFFT();

// store readings from sensors


// line output to serial
char linebuf_all[200];



// define FFT parameters
#define SAMPLES 256
#define SAMPLING_FREQUENCY 16000
// buffer to read samples into, each sample is 16-bits
short wform[SAMPLES];
// FFT real and imaginary vectors
double vReal[SAMPLES];
double vImag[SAMPLES];

// number of samples read
volatile int samplesRead;

// constrain the APDS readiness loop
short apds_loop;
#define APDS_MAX 50

// final result from FFT
double ftsum = 0.0;

// short pause between sensor reads
short srelax = 40;
short oldx;
int ledState = LOW;

#define SCL_INDEX 0x00
#define SCL_TIME 0x01
#define SCL_FREQUENCY 0x02
#define SCL_PLOT 0x03


const int OutPin  = A0;   // wind sensor analog pin  hooked up to Wind P sensor "OUT" pin
const int TempPin = A2;   // temp sesnsor analog pin hooked up to Wind P sensor "TMP" pin



void setup() {
  Serial.begin(115200);
  delay(100);


  // sound
  PDM.onReceive(onPDMdata);
  delay(100);
  PDM.begin(1, SAMPLING_FREQUENCY);
  delay(100);

  pinMode(LED_BUILTIN, OUTPUT);

  // Let's allow things to settle down.
  delay(100);

Serial.print("Initializing SD card...");

  if (!SD.begin()) {
    Serial.println("initialization failed!");
    return;
  }
  Serial.println("initialization done.");




  
}

void loop() {

 
  // wait for sound samples to be read
  if (samplesRead) {
    delay(srelax);
    for (int i = 0; i < SAMPLES; i++) {
      // load the waveform into the FFT real vector
      vReal[i] = double(wform[i]);
      // FFT imaginary vector is zero
      vImag[i] = 0.0;
    }
    

    // compute the spectrum
    // at the end of the sequence, vReal will contain the spectrum
    FFT.Windowing(vReal, SAMPLES, FFT_WIN_TYP_HAMMING, FFT_FORWARD);
    FFT.Compute(vReal, vImag, SAMPLES, FFT_FORWARD);
    FFT.ComplexToMagnitude(vReal, vImag, SAMPLES);


  


    PrintVector(vReal, (SAMPLES >> 1), SCL_FREQUENCY);

    // read wind
    int windADunits = analogRead(OutPin);
    // Serial.print("RW ");   // print raw A/D for debug
    // Serial.print(windADunits);
    // Serial.print("\t");
    
    
    // wind formula derived from a wind tunnel data, annemometer and some fancy Excel regressions
    // this scalin doesn't have any temperature correction in it yet
    float windMPH =  pow((((float)windADunits - 264.0) / 85.6814), 3.36814);
    myFile = SD.open("new4.txt", FILE_WRITE);
    myFile.print("airflow(m/s),");    
    myFile.print(windMPH*0.44704);
myFile.println();
 clocktime=millis(); 
    myFile.print("clocktime");
    myFile.print(",");
    myFile.println(clocktime);

   
    myFile.println();
     myFile.close();
    

// //Reading the File
//
// myFile = SD.open("new4.txt");
//  if (myFile) {
//    Serial.println("new4.txt:");
//    while (myFile.available()) {
//      Serial.write(myFile.read());
//    }
//    myFile.close();
//  } else {
//    Serial.println("error opening airflow.txt");
//  }
//


    // calculate the sum of all spectral components
    // with log10() to adjust for perceptual scale
    ftsum = 0.0;
    // don't start i at 0, low frequencies are too noisy
    // stop at sR / 2 since the spectrum is repeated symmetrically after that
    // (that's how FFT works)
    for (int i = 8; i < samplesRead / 2; i++) {
      ftsum += log10(vReal[i]);
    }

    // clear the samples read count
    samplesRead = 0;
  }

  // prepare the line output with all data
//  sprintf(linebuf_all,
//    "n,%u",
//    
//    int(ftsum));
//
//  // send data out
//  Serial.println(linebuf_all);

  // blink the LED every cycle
  // (heartbeat indicator)
  ledState = ledState ? LOW: HIGH;
  digitalWrite(LED_BUILTIN,  ledState);

  delay(srelax);
}

void onPDMdata() {
  // query the number of bytes available
  int bytesAvailable = PDM.available();

  // read into the sample buffer
  PDM.read(wform, bytesAvailable);

  // 16-bit, 2 bytes per sample
  samplesRead = bytesAvailable / 2;
}


void PrintVector(double *vData, uint16_t bufferSize, uint8_t scaleType)
{
  for (uint16_t i = 0; i < 9; i++)
  {
    double abscissa;
    /* Print abscissa value */
    switch (scaleType)
    {
      case SCL_INDEX:
        abscissa = (i * 1.0);
  break;
      case SCL_TIME:
        abscissa = ((i * 1.0) / SAMPLING_FREQUENCY);
  break;
      case SCL_FREQUENCY:
        abscissa = ((i * 1.0 * SAMPLING_FREQUENCY) / SAMPLES);
  break;
    }
    // open the file. note that only one file can be open at a time,
  // so you have to close this one before opening another.
  myFile = SD.open("new4.txt", FILE_WRITE);
  // if the file opened okay, write to it:
  if (myFile) {
//    Serial.print("Writing to new4.txt...");
  
    myFile.print(abscissa);
    
    myFile.print(",");
    myFile.println(vData[i], 4);
    // close the file:
    myFile.close();
    //Serial.println("done.");
  } else {
    // if the file didn't open, print an error:
    Serial.println("error opening new4.txt");
    
  }

   
  }


 
}
