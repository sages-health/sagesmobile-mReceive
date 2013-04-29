package org.rapidandroid.test;

import org.rapidandroid.activity.Dashboard;

import android.app.AlertDialog;
import android.test.ActivityInstrumentationTestCase2;

public class DeleteFormTest extends ActivityInstrumentationTestCase2<Dashboard>{
	Dashboard dMember;
	public DeleteFormTest(){
	super("org.android.activity",Dashboard.class);
}

public void setUp() throws Exception{
	super.setUp();
	dMember=getActivity();
	//Erase all form Data from default Form
	sendKeys("MENU");
	sendKeys("DPAD_RIGHT ");
	sendKeys("DPAD_RIGHT ");
	sendKeys("ENTER "+"ENTER");
	AlertDialog dialog = (AlertDialog) dMember.managedDialogs.get(dMember.getEraseId());
	
	//allow observers to see test running before it exits 
	try {
	        Thread.sleep(2000);
	      } catch (InterruptedException e) {
	        e.printStackTrace();
	      }
	 
}
public void testDeleteFormTest(){
	
sendKeys("MENU "+"DPAD_DOWN "+ "DPAD_RIGHT "+"DPAD_RIGHT "+"ENTER "+"ENTER");
AlertDialog dialog = (AlertDialog) dMember.managedDialogs.get(dMember.getDeleteFormId());
try {
    Thread.sleep(2000);
  } catch (InterruptedException e) {
    e.printStackTrace();
  }
}
}
