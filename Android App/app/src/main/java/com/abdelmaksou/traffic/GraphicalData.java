package com.abdelmaksou.traffic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class GraphicalData extends AppCompatActivity {

    // MPAndroid line chart
    LineChart lineChart;

    // edit texts
    EditText time;
    EditText dat;

    // title
    TextView title;

    // time and date
    String dt;
    String tim;

    // index
    String index;

    // result
    TextView result;

    // button
    Button check;

    // date and time parameters
    int yearr, month, day;

    // calender
    final Calendar myCalendar = Calendar.getInstance();

    // interface for accessing data outside firebase listener
    public interface OnGetDataListener {
        public void onStart();
        public void onSuccess(ArrayList<Entry> arrayListP, ArrayList<Entry> arrayListR);
        public void onFailed(DatabaseError databaseError);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_graphical_data);

        // title
        title = findViewById(R.id.textView);

        // button
        check = findViewById(R.id.button);

        // edit texts
        time = findViewById(R.id.editTextTime);
        dat = findViewById(R.id.editTextDate);

        // result
        result = findViewById(R.id.result);

        // chart properties
        lineChart = (LineChart) findViewById(R.id.chart);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(false);

        // date and time arrays
        ArrayList<String> dateArray = new ArrayList<>();
        ArrayList<String> timeArray = new ArrayList<>();

        // data arrays
        ArrayList<Entry> yAxisP = new ArrayList<Entry>();
        ArrayList<Entry> yAxisR = new ArrayList<Entry>();

        // firebase references to real-time database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference real = database.getReference("/Real");
        final DatabaseReference predicted = database.getReference().child("/Predicted");
        final DatabaseReference timeData = database.getReference().child("/Date and Time/time");
        final DatabaseReference dateData = database.getReference().child("/Date and Time/date");

        // data implementation outside the listener
        OnGetDataListener listener = new OnGetDataListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(ArrayList<Entry> arrayListP, ArrayList<Entry> arrayListR) {
                // graphing data
                LineDataSet setR, setP;

                setP = new LineDataSet(yAxisP, "Predicted");
                setR = new LineDataSet(yAxisR, "Real");
                setP.setDrawCircles(false);
                setR.setDrawCircles(false);
                setR.setColor(Color.parseColor("#3498db"));
                setP.setColor(Color.parseColor("#e74c3c"));
                setP.setLineWidth(2);
                setR.setLineWidth(2);
                setR.setDrawFilled(true);
                Drawable real_fill = ContextCompat.getDrawable(GraphicalData.this, R.drawable.real_fill);
                setR.setFillDrawable(real_fill);
                LineData data = new LineData(setR, setP);

                lineChart.setBackgroundColor(Color.BLACK);
                lineChart.setDrawGridBackground(false);
                lineChart.getXAxis().setDrawGridLines(false);
                lineChart.getAxisRight().setDrawLabels(false);
                lineChart.getXAxis().setDrawLabels(true);
                lineChart.getAxisLeft().setTextColor(Color.WHITE);
                lineChart.getXAxis().setTextColor(Color.WHITE);
                lineChart.getLegend().setTextColor(Color.WHITE);
                lineChart.getAxisLeft().setDrawLabels(true);
                lineChart.getAxisLeft().setDrawGridLines(false);
                lineChart.getAxisRight().setDrawGridLines(false);
                lineChart.setDrawBorders(false);
                lineChart.getAxisLeft().setDrawAxisLine(false);
                lineChart.getAxisRight().setDrawAxisLine(false);
                lineChart.getXAxis().setDrawAxisLine(false);
                lineChart.getDescription().setText("Traffic Flow Forecasting");
                lineChart.getDescription().setTextColor(Color.WHITE);
                lineChart.getDescription().setTextSize(10);
                lineChart.zoom(8f, 1f, 0,0);
                lineChart.setFocusable(true);

                lineChart.clear();
                lineChart.setData(data);
                lineChart.animateX(3000);
                lineChart.invalidate();
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
            }
        };

        // reading 'real' data
        real.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapShot : snapshot.getChildren()) {
                    yAxisR.add(new Entry(i, snapShot.getValue(Float.TYPE)));
                    i++;
                }

                // reading 'predicted' data
                predicted.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshott) {
                        int i = 0;
                        for (DataSnapshot snapShot : snapshott.getChildren()) {
                            yAxisP.add(new Entry(i, snapShot.getValue(Float.TYPE)));
                            i++;
                        }
                        // passing both of them to graph them
                        listener.onSuccess(yAxisP, yAxisR);

                        // check
                        check.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dateData.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshotDate) {
                                        int x = 0;
                                        timeData.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshotTime) {
                                                for (DataSnapshot date : snapshotDate.getChildren()) {
                                                    if (date.getValue().toString().equals(dt)) {
                                                        for (DataSnapshot time : snapshotTime.getChildren()) {
                                                            if (Integer.parseInt(time.getValue().toString().split(":")[0]) == Integer.parseInt(tim.split(":")[0]) && Integer.parseInt(time.getValue().toString().split(":")[1]) == Integer.parseInt(tim.split(":")[1])) {
                                                                index = time.getKey();
                                                                int value = Integer.parseInt(snapshott.child(index).getValue().toString());
                                                                if ( value <= 50 )
                                                                {
                                                                    result.setText("LIGHT");
                                                                    title.setTextColor(Color.parseColor("#33CDBF"));
                                                                    check.setBackgroundColor(Color.parseColor("#33CDBF"));
                                                                }
                                                                else if (value <= 100 )
                                                                {
                                                                    result.setText("AVERAGE");
                                                                    title.setTextColor(Color.parseColor("#3498db"));
                                                                    check.setBackgroundColor(Color.parseColor("#3498db"));
                                                                }
                                                                else
                                                                {
                                                                    result.setText("CROWDED");
                                                                    title.setTextColor(Color.parseColor("#E74C3C"));
                                                                    check.setBackgroundColor(Color.parseColor("#E74C3C"));
                                                                }
                                                            } else {
                                                                // time not found
                                                            }
                                                        }
                                                    } else {
                                                        // date not found
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // pick time
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(GraphicalData.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        tim = selectedHour + ":" + selectedMinute;
                        time.setText(tim);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        // pick date
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                yearr = year;
                month = monthOfYear;
                day = dayOfMonth;
                updateLabel();
            }

        };

        dat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(GraphicalData.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dt = sdf.format(myCalendar.getTime());
        dat.setText(dt);
    }
}