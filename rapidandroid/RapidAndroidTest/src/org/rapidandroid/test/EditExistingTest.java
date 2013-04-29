package org.rapidandroid.test;

import org.rapidandroid.activity.FormListView;

import android.test.ActivityInstrumentationTestCase2;

public class EditExistingTest extends ActivityInstrumentationTestCase2<FormListView>{
public EditExistingTest(){
	super("org.rapiandroid.activity",FormListView.class);
}
FormListView flvMember;
@Override
protected void setUp() throws Exception{
	super.setUp();
	flvMember = getActivity();
}

public void testViewForm(){
	sendKeys("DPAD_DOWN "+"ENTER "+"ENTER");
	try{
		Thread.sleep(2000);
	}catch(InterruptedException e){e.printStackTrace();}
}
public void testOutputCSV(){
	sendKeys("DPAD_DOWN ENTER");
}
public void testEditForm(){}
}
