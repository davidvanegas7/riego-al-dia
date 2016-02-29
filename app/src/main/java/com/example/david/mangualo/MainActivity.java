package com.example.david.mangualo;

import android.app.ActionBar;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.UUID;
        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.Handler;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

public class MainActivity extends Activity {

    public TextView txtStringLength;
    public Handler bluetoothIn;

    final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    public Riegos datos_de_riegos;
    public Spinner sCultivo, sTextura, sRiego;
    public EditText tCaudal, tArea;
    public TextView tHumedaArduino;

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(myToolbar);

        txtStringLength = (TextView) findViewById(R.id.textViewHumedad);

        sCultivo = (Spinner) findViewById(R.id.editTextCultivo);
        sTextura = (Spinner) findViewById(R.id.editTextTextura);
        sRiego = (Spinner) findViewById(R.id.editTextRiego);

        tCaudal = (EditText) findViewById(R.id.editTextCaudal);
        tArea = (EditText) findViewById(R.id.editTextArea);

        tHumedaArduino = (TextView) findViewById(R.id.textViewHumedad);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("\r\n");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        if(dataInPrint != ""){
                            txtStringLength.setText("" + (-0.1292 * Integer.parseInt(dataInPrint)+132.3   ) );
                        }
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = "20:15:06:01:24:28";

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void validarFormulas(View view) {
        String valor_profundidad = getResources().getStringArray(R.array.listaprofundidad)[sCultivo.getSelectedItemPosition()];
        String valor_porcentajes = getResources().getStringArray(R.array.listaporcentajes)[sRiego.getSelectedItemPosition()];
        String valor_DA = getResources().getStringArray(R.array.listaDA)[sTextura.getSelectedItemPosition()];
        String valor_CC = getResources().getStringArray(R.array.listaCC)[sTextura.getSelectedItemPosition()];

        String area = tArea.getText().toString();
        String caudal = tCaudal.getText().toString();

        String humedad_arduino = tHumedaArduino.getText().toString();

        if (humedad_arduino != "0" && humedad_arduino != "" ) {
            if (valor_profundidad != "" && valor_porcentajes != "" && valor_DA != "" && valor_CC != ""  && area != "" && area != "0"  ) {
                datos_de_riegos = new Riegos( Integer.parseInt(valor_profundidad), Double.parseDouble(valor_porcentajes),  Double.parseDouble(valor_DA),  Double.parseDouble(valor_CC), Double.parseDouble(area) );

                datos_de_riegos.caudal(Double.parseDouble(caudal));
                datos_de_riegos.humedad_arduino(Double.parseDouble(humedad_arduino));
                datos_de_riegos.calcularTiempo();

                Intent intent = new Intent(MainActivity.this, SecondActivity.class)//.putExtra(Intent.EXTRA_TEXT, forecast);
                        .putExtra("response", datos_de_riegos.retornarResultado());

                startActivity(intent);


            }else {
                Toast.makeText(getApplicationContext(), "Campos vacios", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Sin humedad", Toast.LENGTH_SHORT).show();
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d("x", "mensaje leido: "+readMessage);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    try {
                        Thread.sleep(500);                 //1000 milliseconds is one second.
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method

        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                //finish();

            }
        }
    }
}
