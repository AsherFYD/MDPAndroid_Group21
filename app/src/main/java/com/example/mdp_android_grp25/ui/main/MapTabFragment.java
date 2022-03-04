package com.example.mdp_android_grp25.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mdp_android_grp25.MainActivity;
import com.example.mdp_android_grp25.R;

import org.json.JSONException;

public class MapTabFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "MapFragment";

    private PageViewModel pageViewModel;

    Button resetMapBtn, resetTestBtn, updateButton;
    ImageButton directionChangeImageBtn, exploredImageBtn, obstacleImageBtn, clearImageBtn;
    ToggleButton setStartPointToggleBtn, exploreToggleBtn, fastestToggleBtn;          //, setWaypointToggleBtn;
    //Switch manualAutoToggleBtn;
    private static GridMap gridMap;
    private static long exploreTimer, fastestTimer;
    private static long exploreTimerLastStopped, fastestTimerLastStopped;
    TextView exploreTimeTextView, fastestTimeTextView, robotStatusTextView;
    ImageButton exploreResetBtn, fastestResetBtn;

    Switch dragSwitch;
    Switch changeObstacleSwitch;
    Spinner spinner_imageID;
    Spinner spinner_imageBearing;
    private static boolean autoUpdate = false;

    static String imageID;
    static String imageBearing;
    static boolean dragStatus;
    static boolean changeObstacleStatus;

    // Timer
    public static Handler timerHandler = new Handler();

    public Runnable timerRunnableExplore = new Runnable() {
        @Override
        public void run() {
            long millisExplore = System.currentTimeMillis() - exploreTimer;
            int secondsExplore = (int) (millisExplore / 1000);
            int minutesExplore = secondsExplore / 60;
            secondsExplore = secondsExplore % 60;

            if (MainActivity.stopTimerFlag == false) {
                exploreTimeTextView.setText(String.format("%02d:%02d", minutesExplore,
                        secondsExplore));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    public Runnable timerRunnableFastest = new Runnable() {
        @Override
        public void run() {
            long millisFastest = System.currentTimeMillis() - fastestTimer;
            int secondsFastest = (int) (millisFastest / 1000);
            int minutesFastest = secondsFastest / 60;
            secondsFastest = secondsFastest % 60;

            if (MainActivity.stopWk9TimerFlag == false) {
                fastestTimeTextView.setText(String.format("%02d:%02d", minutesFastest,
                        secondsFastest));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    public static MapTabFragment newInstance(int index) {
        MapTabFragment fragment = new MapTabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_map_config, container, false);

        gridMap = MainActivity.getGridMap();
        final DirectionFragment directionFragment = new DirectionFragment();

        resetMapBtn = root.findViewById(R.id.resetBtn);
        resetTestBtn = root.findViewById(R.id.resetTestBtn);
        setStartPointToggleBtn = root.findViewById(R.id.startpointToggleBtn);
        //setWaypointToggleBtn = root.findViewById(R.id.waypointToggleBtn);
        directionChangeImageBtn = root.findViewById(R.id.changeDirectionBtn);
        //exploredImageBtn = root.findViewById(R.id.exploredImageBtn);
        exploreToggleBtn = (ToggleButton) root.findViewById(R.id.exploreToggleBtn3); //Button to start image rec
        fastestToggleBtn = (ToggleButton) root.findViewById(R.id.fastestToggleBtn3); //Button to start fastest path
        obstacleImageBtn = root.findViewById(R.id.addObstacleBtn);
        clearImageBtn = root.findViewById(R.id.clearImageBtn);
        //manualAutoToggleBtn = root.findViewById(R.id.autoManualSwitch);
        //updateButton = root.findViewById(R.id.updateMapBtn);
        robotStatusTextView = MainActivity.getRobotStatusTextView();
        exploreResetBtn = (ImageButton) root.findViewById(R.id.exploreResetBtn); //Button to start image rec
        fastestResetBtn = (ImageButton) root.findViewById(R.id.fastestResetBtn);

        dragSwitch = root.findViewById(R.id.dragSwitch);
        changeObstacleSwitch = root.findViewById(R.id.changeObstacleSwitch);
        spinner_imageID = root.findViewById(R.id.imageIDSpinner);
        spinner_imageBearing = root.findViewById(R.id.bearingSpinner);
        spinner_imageID.setEnabled(false);
        spinner_imageBearing.setEnabled(false);

        exploreTimeTextView = root.findViewById(R.id.exploreTimeTextView2);
        fastestTimeTextView = root.findViewById(R.id.fastestTimeTextView2);
        fastestTimer = 0;
        exploreTimer = 0;
        exploreTimerLastStopped = 0;
        fastestTimerLastStopped = 0;

        // Create an ArrayAdapter using the string array and a default spinner layout
        // Specify the layout to use when the list of choices appears
        // Apply the adapter to the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.imageID_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_imageID.setAdapter(adapter);

        // Repeat for imageBearing
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this.getContext(),
                R.array.imageBearing_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_imageBearing.setAdapter(adapter2);

        spinner_imageID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> a, View v, int pos, long arg3) {
                imageID = a.getItemAtPosition(pos).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> a) { }
        });

        spinner_imageBearing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> a, View v, int pos, long arg3) {
                imageBearing = a.getItemAtPosition(pos).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> a) { }
        });

        resetMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked resetMapBtn");
                showToast("Resetting Map");
                gridMap.resetMap();
            }
        });

        resetTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked resetTestBtn");
                showToast("Resetting Test");
                gridMap.resetTest();
            }
        });


        // switch for dragging
        dragSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                showToast("Dragging is " + (isChecked ? "on" : "off"));
                dragStatus = isChecked;

                if (dragStatus == true) {
                    // disable imageID and imageBearing and disable setObstacle when drag is on
                    spinner_imageID.setEnabled(false);
                    spinner_imageBearing.setEnabled(false);
                    gridMap.setSetObstacleStatus(false); //disable set obstacle when drag status is on
                    changeObstacleSwitch.setChecked(false);
                }

            }
        });

        // switch for changing obstacle
        changeObstacleSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                showToast("Changing Obstacle is " + (isChecked ? "on" : "off"));
                changeObstacleStatus = isChecked;

                if (changeObstacleStatus == true) {
                    // disable dragging, imageID and imageBearing and disable setObstacle
                    spinner_imageID.setEnabled(false);
                    spinner_imageBearing.setEnabled(false);
                    gridMap.setSetObstacleStatus(false);
                    dragSwitch.setChecked(false);
                }

            }
        });

        setStartPointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setStartPointToggleBtn");
                //After pressing CANCEL, text on the button would be "SET STARTPOINT" --> set start point cancelled
                if (setStartPointToggleBtn.getText().equals("SET STARTPOINT"))
                    showToast("Cancelled selecting starting point");

                    //After pressing SET STARTPOINT, text on the button would be "CANCEL" --> set start point started
                else if (setStartPointToggleBtn.getText().equals("CANCEL") && !gridMap.getAutoUpdate()) {
                    showToast("Please select starting point");
                    dragSwitch.setChecked(false); //disable drag when setting start point
                    changeObstacleSwitch.setChecked(false); //disable change obstacle when setting start point
                    gridMap.setSetObstacleStatus(false); //disable setting new obstacles
                    gridMap.setStartCoordStatus(true); //enable set starting coordinates
                    gridMap.toggleCheckedBtn("setStartPointToggleBtn");

                } else
                    //showToast("Please select manual mode");
                showLog("Exiting setStartPointToggleBtn");
            }
        });

        directionChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked directionChangeImageBtn");
                directionFragment.show(getActivity().getFragmentManager(), "Direction Fragment");
                showLog("Exiting directionChangeImageBtn");
            }
        });

        obstacleImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked obstacleImageBtn");

                //If Off, turn on obstacle plotting
                if (!gridMap.getSetObstacleStatus()) {
                    showToast("Please plot obstacles");
                    gridMap.setSetObstacleStatus(true);
                    spinner_imageID.setEnabled(true);
                    spinner_imageBearing.setEnabled(true);
                    gridMap.toggleCheckedBtn("obstacleImageBtn");
                    gridMap.toggleCheckedBtn("obstacleImageBtn");
                }

                //If On, turn off obstacle plotting
                else if (gridMap.getSetObstacleStatus()) {
                    showToast("Plot obstacles turned off");
                    gridMap.setSetObstacleStatus(false);
                    spinner_imageID.setEnabled(false);
                    spinner_imageBearing.setEnabled(false);
                }

                changeObstacleSwitch.setChecked(false);
                dragSwitch.setChecked(false);
                showLog("obstacle status = " + gridMap.getSetObstacleStatus());
                showLog("Exiting obstacleImageBtn");
            }
        });

        //on Click listener for explore button
        exploreToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreToggleBtn");

                if (exploreToggleBtn.getText().equals("Imagerec START")) { //press stop
                    showToast("Image recognition task has stopped");
                    robotStatusTextView.setText("Auto Movement Stopped");
                    long tempExploreLastStopped = System.currentTimeMillis() - exploreTimer;
                    exploreTimerLastStopped = tempExploreLastStopped;
                    timerHandler.removeCallbacks(timerRunnableExplore);
                    exploreResetBtn.setEnabled(true);
                    exploreResetBtn.setBackgroundColor(Color.WHITE);
                }
                else if (exploreToggleBtn.getText().equals("STOP")) { //press wk8 start
                    String msg = gridMap.getObstacles(); //to get the info to send to the robot
                    //MainActivity.printMessage(msg); //send obstacles to robot
                    MainActivity.stopTimerFlag = false;
                    if (msg == "No obstacles found\n") {
                        //MainActivity.printMessage("Please input obstacles"); //for checklist
                        showToast("Please set obstacles on the map");
                    }
                    else if (GridMap.robotDirection == "None"){
                        showToast("Please set robot on the map");
                    }
                    else {
                        //MainActivity.printMessage("beginExplore"); //for checklist
                        showLog(msg);
                        showToast("Image recognition task has started");
                        //send "obs first before sending obstacles info"
                        //send obstacle info
                        MainActivity.printMessage("obs;"+ msg); //send obstacles to robot only when there is robot and obstacles on map
                        robotStatusTextView.setText("Auto Movement Started");
                    }
                    long tempExplore = System.currentTimeMillis();
                    exploreTimer = tempExplore - exploreTimerLastStopped;
                    timerHandler.postDelayed(timerRunnableExplore, 0);
                    exploreResetBtn.setEnabled(false);
                    exploreResetBtn.setBackgroundColor(Color.GRAY);
                }
                else {
                    showToast("Else statement: " + exploreToggleBtn.getText());
                }
                showLog("Exiting exploreToggleBtn");
            }
        });

        exploreResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreResetBtn");
                showToast("Resetting exploration time...");
                exploreTimeTextView.setText("00:00");
                robotStatusTextView.setText("Ready To Move");
                exploreTimer = 0;
                exploreTimerLastStopped = 0;
                timerHandler.removeCallbacks(timerRunnableExplore);
                showLog("Exiting exploreResetImageBtn");
            }
        });

        fastestResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked fastestResetBtn");
                showToast("Resetting fastest time...");
                fastestTimeTextView.setText("00:00");
                robotStatusTextView.setText("Ready To Move");
                fastestTimer = 0;
                fastestTimerLastStopped = 0;
                timerHandler.removeCallbacks(timerRunnableFastest);
                showLog("Exiting fastestResetImageBtn");
            }
        });

        /**
         * for week 9 start button
         */
        fastestToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked fastestToggleBtn");
                if (fastestToggleBtn.getText().equals("Fastest START")) { //press stop for wk9 start
                    showToast("Fastest car timer stop!");
                    robotStatusTextView.setText("Fastest Car Stopped");
                    long tempFastestLastStopped = System.currentTimeMillis() - fastestTimer;
                    fastestTimerLastStopped = tempFastestLastStopped;
                    timerHandler.removeCallbacks(timerRunnableFastest);
                    fastestResetBtn.setEnabled(true);
                    fastestResetBtn.setBackgroundColor(Color.WHITE);
                }
                else if (fastestToggleBtn.getText().equals("STOP")) { //press start wk 9
                    showToast("Fastest car timer start!");
                    try {
                        MainActivity.printMessage("Fastest path start"); //send message for fastest path
                    } catch (Exception e) {
                        showLog(e.getMessage());
                    }
                    MainActivity.stopWk9TimerFlag = false;
                    robotStatusTextView.setText("Fastest Car Started");
                    long tempFastest = System.currentTimeMillis();
                    fastestTimer = tempFastest - fastestTimerLastStopped;
                    timerHandler.postDelayed(timerRunnableFastest, 0);
                    fastestResetBtn.setEnabled(false);
                    fastestResetBtn.setBackgroundColor(Color.GRAY);
                }
                else
                    showToast(fastestToggleBtn.getText().toString());
                showLog("Exiting fastestToggleBtn");
            }
        });

        return root;

        /**
        manualAutoToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked manualAutoToggleBtn");
                if (manualAutoToggleBtn.getText().equals("MANUAL")) {
                    try {
                        gridMap.setAutoUpdate(true);
                        autoUpdate = true;
                        gridMap.toggleCheckedBtn("None");
                        updateButton.setClickable(false);
                        updateButton.setTextColor(Color.GRAY);
                        manualAutoToggleBtn.setText("AUTO");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("AUTO mode");
                }
                else if (manualAutoToggleBtn.getText().equals("AUTO")) {
                    try {
                        gridMap.setAutoUpdate(false);
                        autoUpdate = false;
                        gridMap.toggleCheckedBtn("None");
                        updateButton.setClickable(true);
                        updateButton.setTextColor(Color.BLACK);
                        manualAutoToggleBtn.setText("MANUAL");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("MANUAL mode");
                }
                showLog("Exiting manualAutoToggleBtn");
            }
        });
         **/

        /**
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked updateButton");

                gridMap.imageBearings.get(9)[5] = "South";
                gridMap.imageBearings.get(15)[15] = "South";
                gridMap.imageBearings.get(14)[7] = "West";
                gridMap.imageBearings.get(4)[15] = "West";
                gridMap.imageBearings.get(9)[12] = "East";
                gridMap.setObstacleCoord(5+1, 9+1);
                gridMap.setObstacleCoord(15+1, 15+1);
                gridMap.setObstacleCoord(7+1, 14+1);
                gridMap.setObstacleCoord(15+1, 4+1);
                gridMap.setObstacleCoord(12+1, 9+1);

                gridMap.invalidate();
                showLog("Exiting updateButton");
            }
        });
         **/

        /**
         setWaypointToggleBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        showLog("Clicked setWaypointToggleBtn");
        if (setWaypointToggleBtn.getText().equals("WAYPOINT"))
        showToast("Cancelled selecting waypoint");
        else if (setWaypointToggleBtn.getText().equals("CANCEL")) {
        showToast("Please select waypoint");
        gridMap.setWaypointStatus(true);
        gridMap.toggleCheckedBtn("setWaypointToggleBtn");
        }
        else
        showToast("Please select manual mode");
        showLog("Exiting setWaypointToggleBtn");
        }
        });
         **/
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}

