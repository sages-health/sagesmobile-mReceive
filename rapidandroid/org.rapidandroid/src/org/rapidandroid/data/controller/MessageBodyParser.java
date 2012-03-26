/**
 * 
 */
package org.rapidandroid.data.controller;

import java.util.HashMap;
import java.util.Map;


/**
 * @author POKUAM1
 * @created Feb 2, 2012
 */
public class MessageBodyParser {

	/**
	 * @author POKUAM1
	 * @created Feb 2, 2012
	 */
	public class SagesPdu {

		private int segmentNumber;
		private int totalSegments;
		private long txId;
		private String payload;
		private String sender;
		
		/**
		 * @param segNum
		 * @param totSegs
		 * @param txId2
		 * @param payload2
		 */
		public SagesPdu(int segNum, int totSegs, long txId2, String payload, String sender) {
			this.segmentNumber = segNum;
			this.totalSegments = totSegs;
			this.txId = txId2;
			this.payload = payload;
			this.sender = sender;
		}
		
		public int getSegmentNumber() {
			return segmentNumber;
		}
		public void setSegmentNumber(int segmentNumber) {
			this.segmentNumber = segmentNumber;
		}
		public int getTotalSegments() {
			return totalSegments;
		}
		public void setTotalSegments(int totalSegments) {
			this.totalSegments = totalSegments;
		}
		public long getTxId() {
			return txId;
		}
		public void setTxId(long txId) {
			this.txId = txId;
		}
		public String getPayload() {
			return payload;
		}
		public void setPayload(String payload) {
			this.payload = payload;
		}
		public String getSender() {
			return sender;
		}
		public void setSender(String sender) {
			this.sender = sender;
		}
	}

	public static SagesPdu extractSegmentAsPdu(String msg, String sender){
//		HashMap segmentAttributes = new HashMap<String, String>();
		SagesPdu pdu = extractPdu(msg, sender);
		
//		segmentAttributes.put("totSegs", pdu.getTotalSegments());
//		segmentAttributes.put("segNum", pdu.getSegmentNumber());
//		segmentAttributes.put("txId", pdu.getTxId());
//		segmentAttributes.put("payload", pdu.getPayload());
		
		return pdu;
	}

	/**
	 * @param msg
	 * @return
	 */
	private static SagesPdu extractPdu(String msg, String sender) {
		String splitMsg = ":";
		String splitPdu = ",";
		String[] msgSplit = msg.split(splitMsg);
		
		if (msgSplit.length == 1){
			return null;
		}
		String payload = msgSplit[1];
		String[] pdu = msgSplit[0].split(splitPdu);
		int segNum = Integer.valueOf(pdu[0]).intValue();
		int totSegs = Integer.valueOf(pdu[1]).intValue();
		long txId = Long.valueOf(pdu[2]).longValue();
		
		MessageBodyParser mbd = new MessageBodyParser();
		SagesPdu sagesPdu = mbd.new SagesPdu(segNum, totSegs, txId, payload, sender);
		return sagesPdu;
	}
}
