package com.agestic.flickr.xml.sax;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.agestic.flickr.beans.PhotoBean;

public class SAXXMLHandler extends DefaultHandler {

	private List<PhotoBean> photoBeans;
	Boolean currentElement = false;
	String currentValue = null;
	// to maintain context
	private PhotoBean photoBean;
	private final Stack<String> tagsStack = new Stack<String>();
	private static final String TAG_ENTRY = "entry";
	private static final String TAG_TITLE = "title";
	private static final String TAG_UPDATED = "updated";
	private static final String TAG_PUBLISHED = "published";

	public SAXXMLHandler() {
		photoBeans = new ArrayList<PhotoBean>();
	}

	public List<PhotoBean> getPhotoBeans() {
		return photoBeans;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		currentElement = true;
		pushTag(qName);
		if (qName.equalsIgnoreCase("entry")) {
			photoBean = new PhotoBean();
		} else if (qName.equalsIgnoreCase("link")
				&& attributes.getValue("rel").equals("enclosure")) {
			photoBean.setIcon(attributes.getValue("href"));
			
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		currentElement = false;

		String tag = peekTag();
		if (!qName.equals(tag)) {
			throw new InternalError();
		}

		popTag();
		String parentTag = peekTag();

		if (TAG_TITLE.equalsIgnoreCase(tag)) {

			if (TAG_ENTRY.equalsIgnoreCase(parentTag)) {
				photoBean.setTitle(currentValue);
			}
		} else if (TAG_PUBLISHED.equalsIgnoreCase(tag)) {
			if (TAG_ENTRY.equalsIgnoreCase(parentTag))
			photoBean.setPublished(currentValue);
		} else if (TAG_UPDATED.equalsIgnoreCase(tag)) {
			if (TAG_ENTRY.equalsIgnoreCase(parentTag))
				photoBean.setUpdated(currentValue);
		} else if (qName.equalsIgnoreCase("name")) {
			photoBean.setName(currentValue);

		} else if (TAG_ENTRY.equalsIgnoreCase(tag)) {
			photoBeans.add(photoBean);
		}

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (currentElement) {
			currentValue = new String(ch, start, length);
			currentElement = false;
		}
	}

	 public void startDocument() {
	        pushTag("");
	    }
	
	private void pushTag(String tag) {
		tagsStack.push(tag);
	}

	private String popTag() {
		return tagsStack.pop();
	}

	private String peekTag() {
		return tagsStack.peek();
	}

}
