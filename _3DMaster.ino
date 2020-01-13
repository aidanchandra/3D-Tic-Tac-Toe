#include <Wire.h>
int incomingByte = 0;


boolean initialStates[64];
boolean blueState[64];
boolean orangeState[64];

int IDMap[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 51, 55, 59, 63, 50, 54, 58, 62, 49, 53, 57, 61, 48, 52, 56, 60};

void setup()
{
  Wire.begin();
  Serial.begin(9600);
  pinMode(13, OUTPUT);
  //printTest(10,11);
  for (int i = 0; i < 64; i++) {
    reset(i);
    blueState[i] = false;
    orangeState[i] = false;
  }
}

void loop()
{
  digitalWrite(13, LOW);
  /*
    Command Structure:
    t - scan the i2c line
    ABC
    AB: 00-64, address, addresses <9 must be 01,02..

    C: Command
      Control Commands:
         0: Blue On
           Reply: None
         1: Blue Off
           Reply: None
         2: Orange On
           Reply: None
         3: Orange Off
           Reply: None

         4: Reset Register
           Reply: None
         5: Get Blue Status
           Reply: None
         9: Get Orange Status
           Reply: None
         6: Reset All Registers
           Reply: None
         7: Wait for a button to be pressed, light up node with provided color. Stop message "s"
           Reply: Coordinates ID of the pressed button
           Address:
              00: Blue
              01: Orange
              02: Blue OFF
              03: Orange OFF
       Animation Commands:
         8: Perform an animation specified by address
            Reply: None
            Address:
              00: Scan
              01:
              02:
              03:
              04:
              05:
  */
  if (Serial.available() > 0) {
    String temp = "";
    while (Serial.available() > 0) {
      // read the incoming byte:
      incomingByte = Serial.read();
      temp += (char)incomingByte;
      delay(2);
    }
    int address = temp.substring(0, 2).toInt();
    int command = temp.substring(2, 3).toInt();

    //Serial.print("Address: " + (String)address + "   ");
    //Serial.println("Command: " + (String)command);

    String returnable = "";
    if (temp.substring(0, 2).equals("t")) {
      addressTest();
    }
    else {
      int iterationCount;
      switch (command) {
        case 0:
          blueOn(address);
          break;
        case 1:
          blueOff(address);
          break;
        case 2:
          orangeOn(address);
          break;
        case 3:
          orangeOff(address);
          break;
        case 4:
          reset(address);
          break;
        case 5:
          Serial.println((int)(blueState[getID(address)]));
          break;
        case 6:
          for (int i = 0; i < 64; i++) {
            reset(i);
            blueState[i] = false;
            orangeState[i] = false;
          }
          break;
        case 7:
          for (int i = 0; i < 64; i++) {
            if (initialStates[getID(i)] != hasPressed(i)) {
              delay(1);
              switch (address) {
                case 0:
                  blueOn(i);
                  break;
                case 1:
                  orangeOn(i);
                  break;
                case 2:
                  blueOff(i);
                  break;
                case 3:
                  orangeOff(i);
                  break;

              }
              clearRegister(i);
              for (int j = 0; j < 50; j++) {
                if (i < 10)
                  Serial.println("*0" + (String)i + "*");
                else
                  Serial.println("*" + (String)i + "*");
                delay(10);
              }
              regenerateInitialStates();
              Serial.flush();
              return;
            }
          }
          delay(80);
          for (int j = 0; j < 10; j++) {
            Serial.println("-1");
            delay(10);
          }
          break;
        case 8:
          animation(address);
          break;
        case 9:
          Serial.println((int)(orangeState[getID(address)]));
          break;
      }
    }
    Serial.flush();
  }

}

void printTest(int addressone, int addresstwo) {
  Serial.println("Blue #1 ON!");
  blueOn(addressone);
  delay(3000);
  Serial.println("Blue #2 ON!");
  blueOn(addresstwo);
  delay(3000);
  Serial.println("Blue #1 OFF!");
  blueOff(addressone);
  delay(3000);
  Serial.println("Blue #2 OFF!");
  blueOff(addresstwo);
  delay(3000);


  Serial.println("Orange #1 ON!");
  orangeOn(addressone);
  delay(3000);
  Serial.println("Orange #2 ON!");
  orangeOn(addresstwo);
  delay(3000);
  Serial.println("Orange #1 OFF!");
  orangeOff(addressone);
  delay(3000);
  Serial.println("Orange #2 OFF!");
  orangeOff(addresstwo);
  delay(3000);


  Serial.println("Button 1 State: " + (String)hasPressed(addressone));
  delay(3000);
  Serial.println("Press Button 1 !");
  delay(5000);
  Serial.println("Button 1 State NOW: " + (String)hasPressed(addressone));
  delay(3000);
  Serial.println("Resetting Buttonstate 1 ");
  reset(addressone);
  delay(3000);
  Serial.println("Button 1 State NOW: " + (String)hasPressed(addressone));
  delay(3000);

  Serial.println("Button 2 State: " + (String)hasPressed(addresstwo));
  delay(3000);
  Serial.println("Press Button 2 !");
  delay(5000);
  Serial.println("Button 2 State NOW: " + (String)hasPressed(addresstwo));
  delay(3000);
  Serial.println("Resetting Buttonstate 2 ");
  reset(addresstwo);
  delay(3000);
  Serial.println("Button 2 State NOW: " + (String)hasPressed(addresstwo));
  delay(3000);

  Serial.println("Re-Starting");
  Serial.println();
}

void blueOn(byte add) {
  int address = getID(add);
  Wire.beginTransmission(address);
  Wire.write('a');
  Wire.endTransmission();
  blueState[(int)add] = true;
  if ((int)address == 0) {
    for (int i = 1; i < 64; i++) {
      if (!blueState[i])
        blueOff(i);
    }
  }
}
void blueOff(byte add) {
  int address = getID(add);
  Wire.beginTransmission(address);
  Wire.write('b');
  Wire.endTransmission();
  blueState[(int)add] = false;
  if ((int)address == 0) {
    for (int i = 1; i < 64; i++) {
      if (blueState[i])
        blueOn(i);
    }
  }
}
void orangeOn(byte add) {
  int address = getID(add);
  Wire.beginTransmission(address);
  Wire.write('c');
  Wire.endTransmission();
  orangeState[(int)add] = true;
  if ((int)address == 0) {
    for (int i = 1; i < 64; i++) {
      if (!orangeState[i])
        orangeOff(i);
    }
  }
}
void orangeOff(byte add) {
  int address = getID(add);
  Wire.beginTransmission(address);
  Wire.write('d');
  Wire.endTransmission();
  orangeState[(int)add] = false;
  if ((int)address == 0) {
    for (int i = 1; i < 64; i++) {
      if (orangeState[i])
        orangeOn(i);
    }
  }
}
void clearRegister(byte add) {
  int address = getID(add);
  Wire.beginTransmission(address);
  Wire.write('e');
  Wire.endTransmission();
}
void reset(byte add) {
  int address = getID(add);
  blueOff(add);
  orangeOff(add);
  Wire.beginTransmission(address);
  Wire.write('e');
  Wire.endTransmission();
}
boolean hasPressed(byte add) {
  int address = getID(add);
  Wire.requestFrom(address, 1);    // request 6 bytes from slave device #2
  char c = Wire.read();    // receive a byte as character
  return (c == 'o');
}
void addressTest() {
  byte error, address;
  int nDevices;

  Serial.println("Scanning...");

  nDevices = 0;
  for (address = 0; address < 127; address++ )
  {
    // The i2c_scanner uses the return value of
    // the Write.endTransmisstion to see if
    // a device did acknowledge to the address.
    Wire.beginTransmission(address);
    error = Wire.endTransmission();

    if (error == 0)
    {
      Serial.print("I2C device found at address 0x");
      if (address < 16)
        Serial.print("0");
      Serial.print(address, HEX);
      Serial.print(" ");
      Serial.print(address, DEC);
      Serial.println("  !");

      nDevices++;
    }
    else if (error == 4)
    {
      Serial.print("Unknow error at address 0x");
      if (address < 16)
        Serial.print("0");
      Serial.print(address, HEX);
      Serial.print(" ");
      Serial.println(address, DEC);
    }
  }
  if (nDevices == 0)
    Serial.println("No I2C devices found\n");
  else
    Serial.println("done\n");
}

void animation(int address) {
  for (int i = 0; i < 64; i++) {
    blueOn(i);
    if (i - 1 >= 0) {
      blueOff(i - 1);
      orangeOn(i - 1);
    }
    if (i - 2 >= 0) {
      orangeOff(i - 2);
    }
    delay(80);
  }
}
void shortTest(byte address) {
  blueOn(address);
  delay(1000);
  blueOff(address);
  delay(1000);
  orangeOn(address);
  delay(1000);
  orangeOff(address);
  delay(1000);
  for (int i = 0; i < 3; i++) {
    Serial.print("Button Status");
    Serial.println((String)hasPressed(address));
    delay(1000);
  }
}
int getID(int address) {
  return IDMap[address];
}
int getAddress(int ID) {
  for (int i = 0; i < IDMap; i++) {
    if (ID == IDMap[i]) {
      return i;
    }
  }
}
void regenerateInitialStates() {
  for (int i = 0; i < 64; i++) {
    initialStates[getID(i)] = hasPressed(i);
  }
}

