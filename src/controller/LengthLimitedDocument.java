package controller;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
//this class is used to limit a text field to a certain number of characters
class LengthLimitedDocument extends PlainDocument {
	private int limit;
	  
	LengthLimitedDocument(int limit) {
		super();
		this.limit = limit;
	}
	//inserts the string into the document so long as it does not go past the character limit
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if(str == null)
			return;
	    if((getLength() + str.length()) <= limit) {
	    	super.insertString(offset, str, attr);
	    }
	}
}
