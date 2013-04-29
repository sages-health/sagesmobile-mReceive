/**
 * 
 */
package org.rapidandroid.test;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.test.mock.MockContext;

/**
 * @author powelnv1
 *
 */
public class TestContext extends MockContext{
private List<Intent> mReceivedIntents = new ArrayList<Intent>();

@Override
public String getPackageName(){
	return "org.rapidandroid.test";
}

@Override
public void startActivity(Intent xiIntent){
	mReceivedIntents.add(xiIntent);
}

public List<Intent> getReceivedIntents(){
	return mReceivedIntents;
}

}
