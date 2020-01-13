import arduino.*;
import com.fazecast.jSerialComm.*;
import java.util.concurrent.TimeUnit;

/*
    For use with physical board, running the _3DMaster.ino arduino file

    Main method is free play
*/

public class SerialManager {
    boolean debug = true;
    Arduino master;
    
    
    /*
        Free play, first player being blue, alternating between blue and orange
        No goal state detection
    */
    public static void main(String[] args) throws InterruptedException{
        SerialManager manager = new SerialManager();
        while(true){
            manager.waitForMove(0,100);
            manager.waitForMove(1, 100);
        }
        
    }
    
    
    /*
        Initializes the serial port to an Arduino
    */
    public SerialManager() throws InterruptedException{
        master = new Arduino();
        SerialPort[] a = SerialPort.getCommPorts();
        String port = "";
        for(int i = 0; i < a.length; i++){
            if(a[i].getDescriptivePortName().contains("Arduino"))
                port = a[i].getSystemPortName();
        }
        if(port.equals("")){
            System.out.println("Board not found");
            System.exit(0);
        }
        master.setPortDescription(port);
        master.setBaudRate(9600);
        boolean setConnection = master.openConnection();
        if(debug) System.out.println("Connection status: " + setConnection); 
        TimeUnit.SECONDS.sleep(2);
    }
    
    /*
        Closes the serial port
    */
    public void closePort(){
        master.closeConnection();
    }
    
    
    /*
        Sets the piece at the provided coordinate to the provided color
        
        Color Options:
            0: blue on
            1: orange on
            2: blue off
            3: orange off
    */
    public void move(Coordinate cord, int color) throws InterruptedException{
        int x = cord.x;
        int y = cord.y;
        int z = cord.z;
        
        int address = -1;
        int count = 0;
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                for(int k = 0; k < 4; k++){
                    if(i == x && y == j && z == k){
                        address = count;
                        break;
                    }
                    count++;
                }
            }
        }
        int data = -1;
        switch(color){
            case 0:
                data = 0;
                break;
            case 1:
                data = 2;
                break;
            case 2:
                data = 1;
                break;
            case 3:
                data = 3;
                break;
        }
        if(debug){
            System.out.println("Address: " + address);
            System.out.println("Data: " + data);

        }
        String command = String.valueOf(address) + String.valueOf(data);
        if(address < 10)
            command = "0" + command;
        master.serialWrite(command,1,1);
        TimeUnit.SECONDS.sleep(1);
    }
    
    /*
        Waits for a button to be pressed on the board and updates the board color
            to the provided color.
        Returns the coordinate of the pressed piece
        
        Color Options:
            0: blue on
            1: orange on
            2: blue off
            3: orange off
    */
    public Coordinate waitForMove(int color, int timeout) throws InterruptedException{
        /*
        String recieved = "-1";
        while(recieved.contains("-1")){
            master.serialWrite("0"+String.valueOf(color)+"7",1,1);
            TimeUnit.SECONDS.sleep(1);
            recieved = master.serialRead(1);
            int index = recieved.indexOf("*");
            recieved = recieved.substring(index+1,index+3);
            if(debug) System.out.println("Recieved: " + recieved);
            if(recieved.contains("-1"))
                if(debug) System.out.println("Restarting");
        }
        */
        String recieved = "";
        
        int count = 0;
        while(count < timeout * 10){
            master.serialWrite("0"+String.valueOf(color)+"7",1,1);
            TimeUnit.MILLISECONDS.sleep(10);
            recieved = master.serialRead(1);
            if(!recieved.contains("-1") && recieved.contains("*") && recieved.length()>4){
                System.out.println("Recieved: " + recieved);
                break;
            }
            count++;
        }
        
        int a = parseInt(recieved);
        System.out.println("A: " + a);
        count = 0;
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                for(int k = 0; k < 4; k++){
                    if(a == count){
                        return new Coordinate(i,j,k);
                    }
                    count++;
                }
            }
        }
        return new Coordinate(-1,-1,-1);
    }
    
    /*
        Helper method for waitForMove to parse int out of serial garbled message
    */
    private int parseInt(String input){
        
        for(int i = 0; i < input.length() - 1; i++){
            if(input.substring(i,i+1).equals("*") && isNumber(input.substring(i+1, i+2)) && isNumber(input.substring(i+2, i+3)) && input.substring(i+3, i+4).equals("*")){
                return Integer.valueOf(input.substring(i+1,i+3));
            }
            
        }
        return -1;
    }
    
    /*
        Completely resets the board
    */
    public void resetBoard() throws InterruptedException{
        master.serialWrite("0"+"0"+"6",1,1);
        TimeUnit.SECONDS.sleep(1);
    }
    private boolean isNumber(String str){
        int[] numbers = new int[]{1,2,3,4,5,6,7,8,9,0};
        for(int i = 0; i < numbers.length; i++){
            if(str.equals(String.valueOf(numbers[i]))){
                return true;
            }
        }
        return false;
    }
}
