package com.example.mdp_android_grp25;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.mdp_android_grp25.ui.main.Bluetooth.BluetoothConnectionService;
import com.example.mdp_android_grp25.ui.main.Bluetooth.BluetoothPage;
//import com.example.mdp_android_grp25.ui.main.BluetoothChatFragment;
import com.example.mdp_android_grp25.ui.main.ControlFragment;
import com.example.mdp_android_grp25.ui.main.GridMap;
import com.example.mdp_android_grp25.ui.main.SectionsPagerAdapter;
import com.example.mdp_android_grp25.ui.main.MapTabFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    private static GridMap gridMap;
    private ControlFragment controlFragment;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView, commandLog;
    static TextView robotStatusTextView, bluetoothStatus, bluetoothDevice;
    static Button upBtn, downBtn, leftBtn, rightBtn, sendEnd, sendK;
    static ImageView imageIDView;
    private static MapTabFragment mapTabFragment;

    ImageButton exploreResetBtn;
    ImageButton fastestResetBtn;

    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;

    String obstacleID = "";
    String imageID = "";

    private static final String TAG = "Main Activity";
    public static boolean stopTimerFlag = false;
    public static boolean stopWk9TimerFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //this is for the bottom fragment
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(9999);
        //TabLayout tabs = findViewById(R.id.tabs);
        //tabs.setupWithViewPager(viewPager);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        this.sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        //Bluetooth Button
        Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(MainActivity.this, BluetoothPage.class);
                startActivity(popup);
            }
        });

        // Bluetooth Status
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        bluetoothDevice = findViewById(R.id.bluetoothConnectedDevice);

        //Command Log
        commandLog = findViewById(R.id.commandlog_textview);
        commandLog.setMovementMethod(new ScrollingMovementMethod()); //to make it scrollable

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatus);

        if(BluetoothConnectionService.BluetoothConnectionStatus == true){
            showLog("Update robotstatusTextView ready to move");
            robotStatusTextView.setText("Ready to Move");
        }else{
            showLog("Update robotstatusTextView disconnected");
            robotStatusTextView.setText("Disconnected");
        }

        //For testing purposes
        sendEnd = findViewById(R.id.pressEnd);
        sendEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printMessage("end\n");
            }
        });

        sendK = findViewById(R.id.sendK);
        sendK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printMessage("k\n");
            }
        });

        imageIDView = findViewById(R.id.displayimageview);

        // Controller & on click listeners for controller
        upBtn = findViewById(R.id.upBtn);
        downBtn = findViewById(R.id.downBtn);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);
        exploreResetBtn = (ImageButton) findViewById(R.id.exploreResetBtn); //Button to start image rec
        fastestResetBtn = (ImageButton) findViewById(R.id.fastestResetBtn); //Button to start image rec

        //if robot is not on map --> notify user to put robot on map --> don't send message
        //if movement will make robot out of bound --> notify user that robot is unable to make the movement --> don't send message
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Up Button pressed");
                //printMessage("f");
                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("forward");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Moving forward", Toast.LENGTH_SHORT).show();
                        printMessage("W\n");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to move forward", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }
                showLog("Exiting moving Forward");

            }
        });

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Back Button pressed");
                //printMessage("b");
                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("back");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Moving backward", Toast.LENGTH_SHORT).show();
                        printMessage("S\n");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to move backward", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }
                showLog("Exiting moving backwards");
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Left Button pressed");
                //printMessage("tl");

                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("left");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Turning left", Toast.LENGTH_SHORT).show();
                        printMessage("A\n");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to turn left", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }

                showLog("Exiting left button pressed");

                /**
                if (gridMap.getAutoUpdate())
                    Toast.makeText(MainActivity.this, "Press Manual", Toast.LENGTH_SHORT).show(); //what is thisss
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("left");
                    MainActivity.refreshLabel();
                    Toast.makeText(MainActivity.this, "Turning left", Toast.LENGTH_SHORT).show();
                    MainActivity.printMessage("sl");
                }
                else
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                 **/
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Right Button pressed");
                //printMessage("tr");

                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()){
                    gridMap.moveRobot("right");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Turning right", Toast.LENGTH_SHORT).show();
                        printMessage("D\n");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to turn right", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }

                showLog("Exiting right button pressed");

                /**
                if (gridMap.getAutoUpdate())
                    Toast.makeText(MainActivity.this, "Press Manual", Toast.LENGTH_SHORT).show(); //what is thisss
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("right");
                    MainActivity.refreshLabel();
                    Toast.makeText(MainActivity.this, "Turning right", Toast.LENGTH_SHORT).show();
                    MainActivity.printMessage("sr");
                }
                else
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                 **/
            }
        });

        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            "Cancel",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }
        );

        // Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mapView);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionAxisTextView = findViewById(R.id.directionAxisTextView);

        //ControlFragment for Timer
        //controlFragment = new ControlFragment();

        // initialize ITEM_LIST and imageBearings strings
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                gridMap.ITEM_LIST.get(i)[j] = "";
                gridMap.imageBearings.get(i)[j] = "";
                gridMap.IMAGE_LIST.get(i)[j] = "";
            }
        }
    }

    public static GridMap getGridMap() {
        return gridMap;
    }

    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }

    //public static Button getUpBtn() { return upBtn; }
    //public static Button getDownBtn() { return downBtn; }
    //public static Button getLeftBtn() { return leftBtn; }
    //public static Button getRightBtn() { return rightBtn; }

    public static TextView getBluetoothStatus() { return bluetoothStatus; } //KEEP
    public static TextView getConnectedDevice() { return bluetoothDevice; } //KEEP

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    // Use this method to send message through bluetooth to robot
    public static void printMessage(String message) {
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();

        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }

        showLog("Exiting printMessage");

        /**
        showLog(message);
        editor.putString("message",
            BluetoothChatFragment.getMessageReceivedTextView().getText() + "\n" + message);
        editor.commit();
        refreshMessageReceived();
         **/
    }

    // Store received message into string
    public static void printMessage(String name, int x, int y) throws JSONException {
        showLog("Entering printMessage");
        sharedPreferences();

        JSONObject jsonObject = new JSONObject();
        String message;

        switch(name) {
            case "waypoint":
                jsonObject.put(name, name);
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                message = name + " (" + x + "," + y + ")";
                break;
            default:
                message = "Unexpected default for printMessage: " + name;
                break;
        }
        //editor.putString("message", BluetoothChatFragment.getMessageReceivedTextView().getText() + "\n" + message);
        editor.commit();
        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        showLog("Exiting printMessage");
    }

    /**
    public static void refreshMessageReceived() {
        BluetoothChatFragment
            .getMessageReceivedTextView()
            .setText(sharedPreferences.getString("message", ""));
    }
     **/

    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
        //printMessage("Direction is set to " + direction);
    }


    //For x,y and direction label of robot
    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    public static void receiveMessage(String message) {
        showLog("Entering receiveMessage");
        sharedPreferences();
        editor.putString("message",
            sharedPreferences.getString("message", "") + "\n" + message);
        editor.commit();
        showLog("Exiting receiveMessage");
    }

    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device now connected to "
                        + mDevice.getName(), Toast.LENGTH_LONG).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
                robotStatusTextView.setText("Ready to move"); //why is this not showinggg
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "
                        + mDevice.getName(), Toast.LENGTH_LONG).show();

                editor.putString("connStatus", "Disconnected");
                robotStatusTextView.setText("Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

    // message handler
    // alg sends x,y,robotDirection,movementAction
    // alg sends ALG,<obstacle id>
    // rpi sends RPI,<image id>
    BroadcastReceiver messageReceiver;

    {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("receivedMessage").trim();
                showLog("receivedMessage: message --- " + message);

                if (message.contains(",")) {
                    String[] cmd = message.split(",");
                    showLog(cmd.toString()); //to get the received message

                    //For checklist C9
                    if(cmd[0].trim().equals("TARGET")){
                        showLog("cmd[0] is Target");
                        obstacleID = cmd[1].trim();
                        imageID = cmd[2].trim();
                        showLog("obstacleID = " + obstacleID);
                        showLog("imageID = " + imageID);
                        gridMap.updateIDFromRpi(obstacleID, imageID);
                        commandLog.append(message + "\n");
                        updateImageView(imageID);
                    }

                    //for robot status
                    else if(cmd[0].trim().equals("status")){
                        showLog("Updating robot status");
                        robotStatusTextView.setText(cmd[1]);
                    }
                    else if(cmd[0].trim().equals("ROBOT")){
                        showLog("MOVEMENT RECEIVED");
                        String movement = cmd[1].trim();
                        showLog(movement);
                        int [] curCoord = gridMap.getCurCoord();

                        if(movement.equals("w")){
                            showLog("w received");
                            if(gridMap.getRobotDirection() == "up"){
                                curCoord[1] += 1;
                            }else if (gridMap.getRobotDirection() == "down"){
                                curCoord[1] -= 1;
                            }else if (gridMap.getRobotDirection() == "left"){
                                curCoord[0] -= 1;
                            }else if (gridMap.getRobotDirection() == "right"){
                                curCoord[0] += 1;
                            }
                            robotStatusTextView.setText("Moving Forward");
                            gridMap.setCurCoord(curCoord[0], curCoord[1], gridMap.getRobotDirection());

                        }else if(movement.equals("a")){
                            showLog("a received");
                            if(gridMap.getRobotDirection() == "up"){
                                gridMap.setRobotDirection("left");
                            }else if (gridMap.getRobotDirection() == "down"){
                                gridMap.setRobotDirection("right");
                            }else if (gridMap.getRobotDirection() == "left"){
                                gridMap.setRobotDirection("down");
                            }else if (gridMap.getRobotDirection() == "right"){
                                gridMap.setRobotDirection("up");
                            }
                            robotStatusTextView.setText("Turning Left");

                        }else if(movement.equals("s")){
                            showLog("s received");
                            if(gridMap.getRobotDirection() == "up"){
                                curCoord[1] -= 1;
                            }else if (gridMap.getRobotDirection() == "down"){
                                curCoord[1] += 1;
                            }else if (gridMap.getRobotDirection() == "left"){
                                curCoord[0] += 1;
                            }else if (gridMap.getRobotDirection() == "right"){
                                curCoord[0] -= 1;
                            }
                            robotStatusTextView.setText("Moving Backwards");
                            gridMap.setCurCoord(curCoord[0], curCoord[1], gridMap.getRobotDirection());
                        }else if(movement.equals("d")){
                            showLog("d received");
                            if(gridMap.getRobotDirection() == "up"){
                                gridMap.setRobotDirection("right");
                            }else if (gridMap.getRobotDirection() == "down"){
                                gridMap.setRobotDirection("left");
                            }else if (gridMap.getRobotDirection() == "left"){
                                gridMap.setRobotDirection("up");
                            }else if (gridMap.getRobotDirection() == "right"){
                                gridMap.setRobotDirection("down");
                            }
                            robotStatusTextView.setText("Turning Right");
                        }
                        commandLog.append(message + "\n");
                    }
                }else if (message.equals("END")) {
                    // if wk 8 btn is checked, means running wk 8 challenge and likewise for wk 9
                    // end the corresponding timer
                    ToggleButton exploreButton = findViewById(R.id.exploreToggleBtn3);
                    ToggleButton fastestButton = findViewById(R.id.fastestToggleBtn3);

                    showLog(message + " received");
                    commandLog.append(message + "\n");
                    showLog(message + " appended");

                    if (exploreButton.isChecked()) {
                        showLog("explorebutton is checked");
                        stopTimerFlag = true;
                        showLog("stopTimerFlag set to true");
                        exploreButton.setChecked(true);
                        showLog("explorebutton set to true");

                        //THIS SHIT CAUSES APP TO CRASH
                        //exploreResetBtn.setEnabled(true);
                        //showLog("exploreresetbtn set to true");

                        robotStatusTextView.setText("Image Rec End");
                        showLog("robotstatustextview set text to auto movement");
                    } else if (fastestButton.isChecked()) {
                        showLog("fastestbutton is checked");
                        stopTimerFlag = true;
                        fastestButton.setChecked(true);
                        showLog("fastestbutton set to true");

                        //THIS SHIT CAUSES APP TO CRASH
                        //fastestResetBtn.setEnabled(true);
                        robotStatusTextView.setText("Fastest Path End");
                    }
                }
                    //NOT IN USE
                    /**
                     else if (cmd[0].equals("ALG") || cmd[0].equals("RPI")) {
                     showLog("cmd[0] is ALG or RPI");
                     if (obstacleID.equals(""))
                     obstacleID = cmd[0].equals("ALG") ? cmd[1] : "";
                     if (imageID.equals(""))
                     imageID = cmd[0].equals("RPI") ? cmd[1] : "";

                     showLog("obstacleID = " + obstacleID);
                     showLog("imageID = " + imageID);

                     // call update fn only when both IDs are obtained
                     if (!(obstacleID.equals("") || imageID.equals(""))) {
                     showLog("imageID and obstacleID not empty");
                     gridMap.updateIDFromRpi(obstacleID, imageID);
                     obstacleID = ""; //reset it to empty after updating
                     imageID = ""; //reset it to empty after updating
                     }
                     commandLog.append(cmd.toString() + "\n");
                     }
                     **/

                    /**
                    else if(cmd[0].equals("ROBOT")){
                        showLog("Updating robot position");
                        int x = Integer.parseInt(cmd[1]);
                        int y = Integer.parseInt(cmd[2]);
                        String direction = cmd[3];
                        gridMap.setCurCoord(x, y, direction); //must get integer for x and y
                        commandLog.append(message + "\n");
                    }
                     **/
                    /**
                    else {
                        // alg sends in cm and float e.g. 100,100,N
                        float x = Integer.parseInt(cmd[1]);
                        float y = Integer.parseInt(cmd[2]);

                        // process received figures to pass into our fn
                        int a = Math.round(x);
                        int b = Math.round(y);
                        a = (a / 10) + 1;
                        b = (b / 10) + 1;

                        String direction = cmd[3];
                        // allow robot to show up on grid if its on the very boundary
                        if (a == 1) a++;
                        if (b == 20) b--;

                        //This is to move the robot, performAlgoCommand
                        if (cmd.length == 4) {
                            String command = cmd[3];

                            // if move forward, reverse, turn on the spot left/right
                            if (command.equals("f") || command.equals("r") || command.equals("sr")
                                    || command.equals("sl")) {
                                showLog("forward, reverse or turn on spot");
                                gridMap.performAlgoCommand(a, b, direction);
                            }
                            // for all other turning cmds
                            else {
                                gridMap.performAlgoTurning(a, b, direction, command);
                            }
                        }
                    }
                     **/
            }
        };
    }

    private void updateImageView(String imageID) {
        showLog("inside method");
        showLog("imageid is: " + imageID);
        switch (imageID.trim()) {
            case ("11"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image11);
                break;
            case ("12"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image12);
                break;
            case ("13"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image13);
                break;
            case ("14"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image14);
                break;
            case ("15"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image15);
                break;
            case ("16"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image16);
                break;
            case ("17"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image17);
                break;
            case ("18"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image18);
                break;
            case ("19"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image19);
                break;
            case ("20"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image20);
                break;
            case ("21"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image21);
                break;
            case ("22"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image22);
                break;
            case ("23"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image23);
                break;
            case ("24"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image24);
                break;
            case ("25"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image25);
                break;
            case ("26"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image26);
                break;
            case ("27"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image27);
                break;
            case ("28"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image28);
                break;
            case ("29"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image29);
                break;
            case ("30"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image30);
                break;
            case ("31"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image31);
                break;
            case ("32"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image32);
                break;
            case ("33"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image33);
                break;
            case ("34"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image34);
                break;
            case ("35"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image35);
                break;
            case ("36"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image36);
                break;
            case ("37"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image37);
                break;
            case ("38"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image38);
                break;
            case ("39"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image39);
                break;
            case ("40"):
                showLog("changing imageView");
                imageIDView.setBackgroundResource(R.drawable.image40);
                break;
            default:
                imageIDView.setBackgroundResource(0);
                showLog("Invalid imageID");
        }
    }


    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = (BluetoothDevice) data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}

