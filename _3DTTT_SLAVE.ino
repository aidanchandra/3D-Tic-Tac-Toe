

#include <TinyWire.h>

#define blue_led_pin 3
#define orange_led_pin 4
#define buttonPin 1

boolean buttonPressed = false;
byte address = 33;

byte command;

void setup() {
  pinMode(blue_led_pin, OUTPUT);
  pinMode(orange_led_pin, OUTPUT);
  pinMode(buttonPin, INPUT_PULLUP);
  digitalWrite(blue_led_pin, LOW);
  digitalWrite(orange_led_pin, LOW);
  TinyWire.begin( address );
  TinyWire.onReceive( onI2CReceive );
  TinyWire.onRequest( onI2CRequest );
}

void loop() {
  if(digitalRead(buttonPin) == LOW){
    buttonPressed = true;
  }
}

/*
I2C Slave Receive Callback:
Note that this function is called from an interrupt routine and shouldn't take long to execute
*/
void onI2CReceive(int howMany){
  // loops, until all received bytes are read
  while(TinyWire.available()>0){
    // toggles the led everytime, when an 'a' is received
    byte recieved = TinyWire.read();
    switch(recieved){
      case 'a': //Turn ON Blue Led
      digitalWrite(blue_led_pin, HIGH);
      break;
      case 'b': //Turn OFF Blue LED
      digitalWrite(blue_led_pin, LOW);
      break;
      case 'c':  //Turn ON Orange LED
      digitalWrite(orange_led_pin, HIGH);
      break;
      case 'd': //Turn OFF Orange LED
      digitalWrite(orange_led_pin, LOW);
      break;
      case 'e': //RESET Button Flag
      buttonPressed = false;
      break;
    }
  }
}
void onI2CRequest() {
  // sends one byte with content 'b' to the master, regardless how many bytes he expects
  // if the buffer is empty, but the master is still requesting, the slave aborts the communication
  // (so it is not blocking)
  if(buttonPressed)
    TinyWire.send('o');
  else{
    TinyWire.send('f');
  }
}
