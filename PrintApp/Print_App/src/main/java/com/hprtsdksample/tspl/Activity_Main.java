package com.hprtsdksample.tspl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.hprtsdksample.zj.R;



import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import HPRTAndroidSDK.HPRTPrinterHelper;
import HPRTAndroidSDK.IPort;
import HPRTAndroidSDK.PublicFunction;

public class Activity_Main extends Activity 
{
	private Context thisCon=null;
	private BluetoothAdapter mBluetoothAdapter;
	private PublicFunction PFun=null;
	
	private Button btnWIFI=null;
	private Button btnBT=null;
	private Button btnUSB=null;
	
	private Spinner spnPrinterList=null;
	private TextView txtTips=null;
	private Button btnOpenCashDrawer=null;
	private Button btnSampleReceipt=null;	
	private Button btn1DBarcodes=null;
	private Button btnQRCode=null;
	private Button btnPDF417=null;
	private Button btnCut=null;
	private Button btnPageMode=null;
	private Button btnImageManage=null;
	private Button btnGetRemainingPower=null;
	
	private EditText edtTimes=null;
	
	private ArrayAdapter arrPrinterList; 
	private static HPRTPrinterHelper HPRTPrinter=new HPRTPrinterHelper();
	private String ConnectType="";
	private String PrinterName="";
	private String PortParam="";
	
	private UsbManager mUsbManager=null;	
	private UsbDevice device=null;
	private static final String ACTION_USB_PERMISSION = "com.HPRTSDKSample";
	private PendingIntent mPermissionIntent=null;
	private static IPort Printer=null;			
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try
		{
			thisCon=this.getApplicationContext();
			
			btnWIFI = (Button) findViewById(R.id.btnWIFI);
			btnUSB = (Button) findViewById(R.id.btnUSB);
			btnBT = (Button) findViewById(R.id.btnBT);
			
			//edtTimes = (EditText) findViewById(R.id.edtTimes);
			
			spnPrinterList = (Spinner) findViewById(R.id.spn_printer_list);	
			txtTips = (TextView) findViewById(R.id.txtTips);
			btnSampleReceipt = (Button) findViewById(R.id.btnSampleReceipt);
			btnOpenCashDrawer = (Button) findViewById(R.id.btnOpenCashDrawer);
			btn1DBarcodes = (Button) findViewById(R.id.btn1DBarcodes);
			btnQRCode = (Button) findViewById(R.id.btnQRCode);
			btnPDF417 = (Button) findViewById(R.id.btnPDF417);
			btnCut = (Button) findViewById(R.id.btnCut);
			btnPageMode = (Button) findViewById(R.id.btnPageMode);
			btnImageManage = (Button) findViewById(R.id.btnImageManage);
			btnGetRemainingPower = (Button) findViewById(R.id.btnGetRemainingPower);
					
			mPermissionIntent = PendingIntent.getBroadcast(thisCon, 0, new Intent(ACTION_USB_PERMISSION), 0);
	        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			thisCon.registerReceiver(mUsbReceiver, filter);
			
			PFun=new PublicFunction(thisCon);
//			InitSetting();
			InitCombox();
			this.spnPrinterList.setOnItemSelectedListener(new OnItemSelectedPrinter());
			//Enable Bluetooth
			EnableBluetooth();
//			String languageEncode = PAct.LanguageEncode();
//			Log.e("TAG", "languageEncode:"+languageEncode);
		}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onCreate ")).append(e.getMessage()).toString());
		}
	}
	
	private void InitSetting()
	{
		String SettingValue="";
		SettingValue=PFun.ReadSharedPreferencesData("Codepage");
		if(SettingValue.equals(""))		
			PFun.WriteSharedPreferencesData("Codepage", "0,PC437(USA:Standard Europe)");			
		
		SettingValue=PFun.ReadSharedPreferencesData("Cut");
		if(SettingValue.equals(""))		
			PFun.WriteSharedPreferencesData("Cut", "0");	//0:禁止,1:打印前,2:打印后
			
		SettingValue=PFun.ReadSharedPreferencesData("Cashdrawer");
		if(SettingValue.equals(""))			
			PFun.WriteSharedPreferencesData("Cashdrawer", "0");
					
		SettingValue=PFun.ReadSharedPreferencesData("Buzzer");
		if(SettingValue.equals(""))			
			PFun.WriteSharedPreferencesData("Buzzer", "0");
					
		SettingValue=PFun.ReadSharedPreferencesData("Feeds");
		if(SettingValue.equals(""))			
			PFun.WriteSharedPreferencesData("Feeds", "0");				
	}
	
	//add printer list
	private void InitCombox()
	{
		try
		{
			arrPrinterList = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
			String strSDKType=thisCon.getString(com.hprtsdksample.zj.R.string.sdk_type);
			if(strSDKType.equals("all"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_other, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("hprt"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_hprt, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mkt"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mkt, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mprint"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mprint, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("sycrown"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_sycrown, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("mgpos"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_mgpos, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("ds"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_ds, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("cst"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_cst, android.R.layout.simple_spinner_item);
			if(strSDKType.equals("other"))
				arrPrinterList=ArrayAdapter.createFromResource(this, R.array.printer_list_other, android.R.layout.simple_spinner_item);
			arrPrinterList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			PrinterName=arrPrinterList.getItem(0).toString();
			spnPrinterList.setAdapter(arrPrinterList);
		}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> InitCombox ")).append(e.getMessage()).toString());
		}
	}
	
	private class OnItemSelectedPrinter implements OnItemSelectedListener
	{				
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{

			PrinterName=arrPrinterList.getItem(arg2).toString();
			HPRTPrinter=new HPRTPrinterHelper(thisCon,PrinterName);
			CapturePrinterFunction();
	//		GetPrinterProperty();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// TODO Auto-generated method stub			
		}
	}
	
	//EnableBluetooth
	private boolean EnableBluetooth()
    {
        boolean bRet = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null)
        {
            if(mBluetoothAdapter.isEnabled())
                return true;
            mBluetoothAdapter.enable();
            try 
    		{
    			Thread.sleep(500);
    		} 
    		catch (InterruptedException e) 
    		{			
    			e.printStackTrace();
    		}
            if(!mBluetoothAdapter.isEnabled())
            {
                bRet = true;
                Log.d("PRTLIB", "BTO_EnableBluetooth --> Open OK");
            }
        } 
        else
        {
        	Log.d("HPRTSDKSample", (new StringBuilder("Activity_Main --> EnableBluetooth ").append("Bluetooth Adapter is null.")).toString());
        }
        return bRet;
    }
	
	//call back by scan bluetooth printer
	@Override  
  	protected void onActivityResult(int requestCode, int resultCode, Intent data)  
  	{  
  		try
  		{  		
  			String strIsConnected;
	  		switch(resultCode)
	  		{
	  			case HPRTPrinterHelper.ACTIVITY_CONNECT_BT:		
	  				String strBTAddress="";
	  				strIsConnected=data.getExtras().getString("is_connected");
	  	        	if (strIsConnected.equals("NO"))
	  	        	{
	  	        		txtTips.setText(thisCon.getString(R.string.activity_main_scan_error));	  	        		
  	                	return;
	  	        	}
	  	        	else
	  	        	{	  	        		
	  						txtTips.setText(thisCon.getString(R.string.activity_main_connected));
	  					return;
	  	        	}		  	        	
	  			case HPRTPrinterHelper.ACTIVITY_CONNECT_WIFI:		
	  				String strIPAddress="";
	  				String strPort="";
	  				strIsConnected=data.getExtras().getString("is_connected");
	  	        	if (strIsConnected.equals("NO"))
	  	        	{
	  	        		txtTips.setText(thisCon.getString(R.string.activity_main_scan_error));	  	        		
  	                	return;
	  	        	}
	  	        	else
	  	        	{	  	        		
	  	        		strIPAddress=data.getExtras().getString("IPAddress");
	  	        		strPort=data.getExtras().getString("Port");
	  	        		if(strIPAddress==null || !strIPAddress.contains("."))	  					
	  						return;	  						  					
	  	        		HPRTPrinter=new HPRTPrinterHelper(thisCon,spnPrinterList.getSelectedItem().toString().trim());
	  					if(HPRTPrinterHelper.PortOpen("WiFi,"+strIPAddress+","+strPort)!=0)	  						  						
	  						txtTips.setText(thisCon.getString(R.string.activity_main_connecterr));	  	                	
	  					else
	  						txtTips.setText(thisCon.getString(R.string.activity_main_connected));
	  					return;
	  	        	}		  	        	
	  			case HPRTPrinterHelper.ACTIVITY_IMAGE_FILE:	  				
//	  		    	PAct.LanguageEncode();
//	  		    	HPRTPrinterHelper.printAreaSize("70", "80");
//	  		    	HPRTPrinterHelper.CLS();
//	  		    	String strImageFile=data.getExtras().getString("FilePath");
//	  		    	HPRTPrinterHelper.printImage("0","0",strImageFile,true);
//	  		    	HPRTPrinterHelper.Print("1", "1");
	  				return;
	  			case HPRTPrinterHelper.ACTIVITY_PRNFILE:	  				
	  				String strPRNFile=data.getExtras().getString("FilePath");
	  				 File file = null;
	  		        try { 
	  		                file = new File(strPRNFile);
	  		        } catch (Exception e) {
	  		                e.printStackTrace();
	  		        }
	  		        FileInputStream fis = new FileInputStream(file);
//	  				HPRTPrinterHelper.PrintBinaryFile(fis);  					  				
	  				
	  				/*String strPRNFile=data.getExtras().getString("FilePath");	  					  				
	  				byte[] bR=new byte[1];
	  				byte[] bW=new byte[3];
	  				bW[0]=0x10;bW[1]=0x04;bW[2]=0x02;
	  				for(int i=0;i<Integer.parseInt(edtTimes.getText().toString());i++)
	  				{
	  					HPRTPrinterHelper.PrintBinaryFile(strPRNFile);
	  					HPRTPrinterHelper.DirectIO(bW, null, 0);
	  					HPRTPrinterHelper.DirectIO(null, bR, 1);	  						
	  				}*/
	  				return;
  			}
  		}
  		catch(Exception e)
  		{
  			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onActivityResult ")).append(e.getMessage()).toString());
  		}
        super.onActivityResult(requestCode, resultCode, data);  
  	} 
	
	@SuppressLint("NewApi")
	public void onClickConnect(View view) 
	{		
//    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
	    	if(HPRTPrinter!=null)
			{					
	    		HPRTPrinterHelper.PortClose();
			}
			
	    	if(view.getId()==R.id.btnBT)
	    	{	
	    		ConnectType="Bluetooth";
				Intent serverIntent = new Intent(thisCon,Activity_DeviceList.class);				
				startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);				
				return;
	    	}
	    	else if(view.getId()==R.id.btnWIFI)
	    	{	    		
	    		ConnectType="WiFi";
	    		Intent serverIntent = new Intent(thisCon,Activity_Wifi.class);
				serverIntent.putExtra("PN", PrinterName); 
				startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_WIFI);				
				return;	
	    	}
	    	else if(view.getId()==R.id.btnUSB)
	    	{
	    		ConnectType="USB";							
				HPRTPrinter=new HPRTPrinterHelper(thisCon,arrPrinterList.getItem(spnPrinterList.getSelectedItemPosition()).toString());					
				//USB not need call "iniPort"				
				mUsbManager = (UsbManager) thisCon.getSystemService(Context.USB_SERVICE);				
		  		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();  		
		  		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		  		
		  		boolean HavePrinter=false;		  
		  		while(deviceIterator.hasNext())
		  		{
		  		    device = deviceIterator.next();
		  		    int count = device.getInterfaceCount();
		  		    for (int i = 0; i < count; i++) 
		  	        {
		  		    	UsbInterface intf = device.getInterface(i); 
		  		    	//Class ID为7表示该USB设备为打印机设备
		  	            if (intf.getInterfaceClass() == 7) 
		  	            {
		  	            	HavePrinter=true;
		  	            	mUsbManager.requestPermission(device, mPermissionIntent);		  	            	
		  	            }
		  	        }
		  		}
		  		if(!HavePrinter)
		  			txtTips.setText(thisCon.getString(R.string.activity_main_connect_usb_printer));	
	    	}
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickConnect "+ConnectType)).append(e.getMessage()).toString());
		}
    }
		   			
	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
	    public void onReceive(Context context, Intent intent) 
	    {
	    	try
	    	{
		        String action = intent.getAction();	       
		        //Toast.makeText(thisCon, "now:"+System.currentTimeMillis(), Toast.LENGTH_LONG).show();
		        //HPRTPrinterHelper.WriteLog("1.txt", "fds");
		        //获取访问USB设备权限
		        if (ACTION_USB_PERMISSION.equals(action))
		        {
			        synchronized (this) 
			        {		        	
			            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
				        {			 
				        	if(HPRTPrinterHelper.PortOpen(device)!=0)
							{					
				        		HPRTPrinter=null;
								txtTips.setText(thisCon.getString(R.string.activity_main_connecterr));												
			                	return;
							}
				        	else
				        		txtTips.setText(thisCon.getString(R.string.activity_main_connected));
				        		
				        }		
				        else
				        {			        	
				        	return;
				        }
			        }
			    }
		        //断开连接
		        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) 
		        {
		            device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		            if (device != null) 
		            {	                	            	
						HPRTPrinterHelper.PortClose();					
		            }
		        }	    
	    	} 
	    	catch (Exception e) 
	    	{
	    		Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> mUsbReceiver ")).append(e.getMessage()).toString());
	    	}
		}
	};
	
	public void onClickClose(View view) 
	{
//    	if (!checkClick.isClickEvent()) return;
    	
    	try
    	{
	    	if(HPRTPrinter!=null)
			{					
	    		HPRTPrinterHelper.PortClose();
			}
			this.txtTips.setText(R.string.activity_main_tips);
			return;	
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickClose ")).append(e.getMessage()).toString());
		}
    }
	
	public void onClickbtnSetting(View view) 
	{
    	
    	try
    	{
//    		startActivity(new Intent(Activity_Main.this, Activity_Setting.class));
    	}
		catch (Exception e) 
		{			
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> onClickClose ")).append(e.getMessage()).toString());
		}
    }
	
	public void onClickDo(View view) 
	{
		
		if(!HPRTPrinterHelper.IsOpened())
		{
			Toast.makeText(thisCon, thisCon.getText(R.string.activity_main_tips), Toast.LENGTH_SHORT).show();				
			return;
		}
		    	    	
    	else if(view.getId()==R.id.btnSampleReceipt)
    	{
    		PrintSampleReceipt();
    	}
    }
	
	
	private void CapturePrinterFunction()
	{
		try
		{
			int[] propType=new int[1];
			byte[] Value=new byte[500];
			int[] DataLen=new int[1];
			String strValue="";
			boolean isCheck=false;
			if (PrinterName.equals("LPG4")|PrinterName.equals("LPQ118")|PrinterName.equals("108B")|PrinterName.equals("R42")|PrinterName.equals("106B")
					|PrinterName.equals("HM-T300")|PrinterName.equals("SM-L300")|PrinterName.equals("HM-E200")|PrinterName.equals("HM-E300")|PrinterName.equals("HM-A300")) {
				btnCut.setVisibility(View.GONE);		
				btnOpenCashDrawer.setVisibility(View.GONE);		
				btn1DBarcodes.setVisibility(View.GONE);		
				btnQRCode.setVisibility(View.GONE);		
				btnPageMode.setVisibility(View.GONE);		
				btnPDF417.setVisibility(View.GONE);		
				btnGetRemainingPower.setVisibility(View.GONE);		
				btnWIFI.setVisibility(View.VISIBLE);		
				btnUSB.setVisibility(View.GONE);		
				btnBT.setVisibility(View.VISIBLE);	
				btnSampleReceipt.setVisibility(View.VISIBLE);	
			}/*else {
				int iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT, propType, Value,DataLen);
				if(iRtn!=0)
					return;			
				PrinterProperty.Cut=(Value[0]==0?false:true);
				btnCut.setVisibility((PrinterProperty.Cut?View.VISIBLE:View.GONE));
				
				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_DRAWER, propType, Value,DataLen);
				if(iRtn!=0)
					return;		
				PrinterProperty.Cashdrawer=(Value[0]==0?false:true);
				btnOpenCashDrawer.setVisibility((PrinterProperty.Cashdrawer?View.VISIBLE:View.GONE));
				
				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_BARCODE, propType, Value,DataLen);
				if(iRtn!=0)
					return;						
				PrinterProperty.Barcode=new String(Value);
				isCheck=PrinterProperty.Barcode.replace("QRCODE", "").replace("PDF417", "").replace(",,", ",").replace(",,", ",").length()>0;
				btn1DBarcodes.setVisibility((isCheck?View.VISIBLE:View.GONE));								
				isCheck = PrinterProperty.Barcode.contains("QRCODE");
				btnQRCode.setVisibility((isCheck?View.VISIBLE:View.GONE));
				btnPDF417.setVisibility((PrinterProperty.Barcode.indexOf("PDF417") != -1?View.VISIBLE:View.GONE));
				
				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE, propType, Value,DataLen);
				if(iRtn!=0)
					return;		
				PrinterProperty.Pagemode=(Value[0]==0?false:true);
				btnPageMode.setVisibility((PrinterProperty.Pagemode?View.VISIBLE:View.GONE));
				
				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_GET_REMAINING_POWER, propType, Value,DataLen);
				if(iRtn!=0)
					return;	
				PrinterProperty.GetRemainingPower=(Value[0]==0?false:true);
				btnGetRemainingPower.setVisibility((PrinterProperty.GetRemainingPower?View.VISIBLE:View.GONE));
				
				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_CONNECT_TYPE, propType, Value,DataLen);
				if(iRtn!=0)
					return;	
				PrinterProperty.ConnectType=(Value[1]<<8)+Value[0];
				btnWIFI.setVisibility(((PrinterProperty.ConnectType&1)==0?View.GONE:View.VISIBLE));
//				btnUSB.setVisibility(((PrinterProperty.ConnectType&16)==0?View.GONE:View.VISIBLE));
				btnBT.setVisibility(((PrinterProperty.ConnectType&32)==0?View.GONE:View.VISIBLE));
				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PRINT_RECEIPT, propType, Value,DataLen);
				if(iRtn!=0)
					return;			
				PrinterProperty.SampleReceipt=(Value[0]==0?false:true);
				btnSampleReceipt.setVisibility((PrinterProperty.SampleReceipt?View.VISIBLE:View.GONE));							
				
			}*/
		}
		catch(Exception e)
		{
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
		}
	}
	
//	private void GetPrinterProperty()
//	{
//		try
//		{
//			int[] propType=new int[1];
//			byte[] Value=new byte[500];
//			int[] DataLen=new int[1];
//			String strValue="";			
//			int iRtn=0;
//			
//			iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_STATUS_MODEL, propType, Value,DataLen);
//			if(iRtn!=0)
//				return;			
//			PrinterProperty.StatusMode=Value[0];
//			
//			if(PrinterProperty.Cut)
//			{
//				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT_SPACING, propType, Value,DataLen);
//				if(iRtn!=0)
//					return;			
//				PrinterProperty.CutSpacing=Value[0];				
//			}
//			else
//			{
//				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_TEAR_SPACING, propType, Value,DataLen);
//				if(iRtn!=0)
//					return;		
//				PrinterProperty.TearSpacing=Value[0];				
//			}	
//			
//			if(PrinterProperty.Pagemode)
//			{
//				iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE_AREA, propType, Value,DataLen);
//				if(iRtn!=0)
//					return;			
//				PrinterProperty.PagemodeArea=new String(Value).trim();				
//			}
//			Value=new byte[500];
//			iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_WIDTH, propType, Value,DataLen);
//			if(iRtn!=0)
//				return;			
//			PrinterProperty.PrintableWidth=(int)(Value[0] & 0xFF | ((Value[1] & 0xFF) <<8));
//		}
//		catch(Exception e)
//		{
//			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
//		}
//	}
	
	private void PrintSampleReceipt()
	{
		try
		{
			HashMap<String, String> pum=new HashMap<String, String>();
			pum.put("[Referred]", "蒙 锡林郭勒盟");
			pum.put("[City]", "锡林郭勒盟 包");
			pum.put("[Number]", "108");
			pum.put("[Receiver]", "渝州");
			pum.put("[Receiver_Phone]", "15182429075");
			pum.put("[Receiver_address1]", "内蒙古自治区 锡林郭勒盟 正黄旗 解放东路与");//收件人地址第一行
			pum.put("[Receiver_address2]", "外滩路交叉口62号静安中学静安小区10栋2单元");//收件人第二行（若是没有，赋值""）
			pum.put("[Receiver_address3]", "1706室");//收件人第三行（若是没有，赋值""）
			pum.put("[Sender]", "洲瑜");
			pum.put("[Sender_Phone]", "13682429075");
			pum.put("[Sender_address1]", "浙江省 杭州市 滨江区 滨盛路1505号1706室信息部,滨盛路1505号滨盛");//寄件人地址第一行
			pum.put("[Sender_address2]", "滨盛路1505号1706室信息部");//寄件人第二行（若是没有，赋值""）
			pum.put("[Barcode]", "998016450402");
			pum.put("[Waybill]", "运单号：998016450402");
			pum.put("[Product_types]", "数码产品");
			pum.put("[Quantity]", "数量：22");
			pum.put("[Weight]", "重量：22.66KG");
			Set<String> keySet = pum.keySet();
			Iterator<String> iterator = keySet.iterator();
			InputStream afis =this.getResources().getAssets().open("TTKD.txt");//打印模版放在assets文件夹里
			String path = new String(InputStreamToByte(afis ),"utf-8");//打印模版以utf-8无bom格式保存
			while (iterator.hasNext()) {
				String string = (String)iterator.next();
				path = path.replace(string, pum.get(string));
			}
			HPRTPrinterHelper.printText(path);
			byte[] byt=new byte[]{0x0c};
			HPRTPrinterHelper.WriteData(byt);
		}
		catch(Exception e)
		{
			Toast.makeText(thisCon, "失败", 0).show();
			Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
		}
	}
	
	private byte[] InputStreamToByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata;
	}
	/*public static class PrinterProperty
	{
		public static String Barcode="";
		public static boolean Cut=false;
		public static int CutSpacing=0;
		public static int TearSpacing=0;
		public static int ConnectType=0;
		public static boolean Cashdrawer=false;
		public static boolean Buzzer=false;
		public static boolean Pagemode=false;
		public static String PagemodeArea="";
		public static boolean GetRemainingPower=false;
		public static boolean SampleReceipt=true;
		public static int StatusMode=0;
	}*/
}
