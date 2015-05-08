package com.yineng.ynmessager.smack;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class ReqIQProvider implements IQProvider {
	public ReqIQProvider() {
	}

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
		ReqIQResult iqProvider = new ReqIQResult();
		iqProvider.setType(IQ.Type.RESULT);
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_TAG) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					// 解析IQ xml 节点
					String name = parser.getName();
//					<iq type="result" id="tV95mrmA" to="xiognshihui@m.com" from="admin@m.com">
//					  <notice xmlns="com:yineng:notice">
//					    <type>orgUpdate</type>
//					  </notice>
//					</iq>
					if ("req".equals(name)) {
						iqProvider.setNameSpace(parser.getNamespace());//命名空间
					} else if ("notice".equals(name)) {
						iqProvider.setNameSpace(parser.getNamespace());
					}
					
					if ("resp".equals(name)) {						
						iqProvider.setResp(parser.nextText());//		
						
					}
					if ("action".equals(name)) {
						iqProvider.setAction(parser.nextText());//
					}
					if ("id".equals(name)) {
						iqProvider.setId(parser.nextText());//message received id
					}
					if ("code".equals(name)) {
						iqProvider.setCode(Integer.parseInt(parser.nextText()));//
					}
					if ("sendTime".equals(name)) {
						iqProvider.setSendTime(parser.nextText());//收到消息回执时间
					}
					if ("type".equals(name)) {
						iqProvider.setTypeStr(parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return iqProvider;
	}
}