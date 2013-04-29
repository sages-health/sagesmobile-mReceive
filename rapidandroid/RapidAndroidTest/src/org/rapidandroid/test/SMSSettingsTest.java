package org.rapidandroid.test;

import org.rapidandroid.activity.GlobalSettings;

import android.test.ActivityInstrumentationTestCase2;

public class SMSSettingsTest extends ActivityInstrumentationTestCase2<GlobalSettings> {
	public SMSSettingsTest(){
		super("org.rapidandroid.activity",GlobalSettings.class);
	}
	GlobalSettings gsMember;
	@Override
	protected void setUp() throws Exception{
		super.setUp();
		try{Thread.sleep(2000);}catch(InterruptedException e){e.printStackTrace();}
		gsMember = getActivity();
	}
	
	public void testSMSSettings(){
		
	}
	

}
