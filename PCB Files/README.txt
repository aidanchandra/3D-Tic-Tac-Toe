Each PCB is essentially a game piece. Each PCB contains a button, two LEDs (Blue
for player 1, Orange for player 2), and an Attiny85. Each gamepice communicates
with an Arduino UNO through i2c and the Arduino UNO relays that information to
the computer over serial. Information relayed includes which gamepieces have
buttons pressed. The computer also sends commands to the Arduino, which in
turn sends i2c commands to the correct gamepiece in order to perform actions such
as light up a certian color LED to indicate the AI's move.
