package com.ibm.compute;
import java.util.List;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbJSON;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.model.Book;


public class ListBooks_JavaCompute extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
//		MbOutputTerminal alt = getOutputTerminal("alternate");
		//MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage();
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ----------------------------------------------------------
			// Add user code below		
/*
{ 
  [ 
	  { "Title": "Steve Jobs", 
		"ISBN": "1451648537",
		"Author": "Walter Isaacson", 
		"Language": "English",
		"Published": "Oct 24, 2011",
		"Format": [ "Hardcover", "Paperback", "Audiobook CD", "Audible" ]
	  }, 
	  { "Name": "Beautiful Whale", 
		"ISBN": "1419703846", 
		"Author": "Bryant Austin, Sylvia Earle",
		"Language": "English", 
		"Format": [ "Hardcover" ] 
	  } 
  ] 
}
*/       
			// JSON Body
			MbElement jsonEle = outMessage.getRootElement().createElementAsLastChild(MbJSON.PARSER_NAME);
			MbElement booksEle = jsonEle.createElementAsLastChild(MbJSON.ARRAY, "Data", null);			
			List<Book> books = Book.list();
			books.forEach(book->{			
				try {
					MbElement bookEle = booksEle.createElementAsLastChild(MbJSON.OBJECT, MbJSON.ARRAY_ITEM_NAME, null);
					bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Id", book.getId().toString());
					bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Title", book.getTitle());
					bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "ISBN", book.getIsbn());
					bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Author", book.getAuthor());
					bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Published", book.getPublishedAsString());
					bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Language", book.getLanguage());
					MbElement bookFormatsEle = bookEle.createElementAsLastChild(MbJSON.ARRAY, "Formats", null);
					book.getFormats().forEach(format->{
						try {
							bookFormatsEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, MbJSON.ARRAY_ITEM_NAME, format); 
						} catch(MbException e) {
							// ignore
						}
					});			
				} catch(MbException e) {
					e.printStackTrace();
				}
			});
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
