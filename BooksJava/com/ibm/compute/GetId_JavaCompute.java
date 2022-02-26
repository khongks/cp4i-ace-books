package com.ibm.compute;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbJSON;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.model.Book;

public class GetId_JavaCompute extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		//MbOutputTerminal alt = getOutputTerminal("alternate");

		//MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage();
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ----------------------------------------------------------
			// Add user code below
	
			// Get Id
			MbMessage localEnvironment = inAssembly.getLocalEnvironment();
			MbElement idEle = localEnvironment.getRootElement().getFirstElementByPath("/REST/Input/Parameters/id");
			MbElement jsonEle = outMessage.getRootElement().createElementAsLastChild(MbJSON.PARSER_NAME);
			// JSON Body
			MbElement bookEle = jsonEle.createElementAsLastChild(MbElement.TYPE_NAME, MbJSON.DATA_ELEMENT_NAME, null);
			Integer id = Integer.decode(idEle.getValueAsString());
			Book book = Book.getBookById(id);
			if(book != null) {
				bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Id", book.getId().toString());
				bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Title", book.getTitle());
				bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "ISBN", book.getIsbn());
				bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Author", book.getAuthor());
				bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Published", book.getPublishedAsString());
				bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Language", book.getLanguage());
				MbElement bookFormatEle = bookEle.createElementAsLastChild(MbJSON.ARRAY, "Format", null);
				book.getFormats().forEach(format->{
					try {
						bookFormatEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, MbJSON.ARRAY_ITEM_NAME, format); 
					} catch(MbException e) {
						// ignore
					}
				});
			} else {
				// HTTP Status Code
				MbElement destEle = localEnvironment.getRootElement().getFirstElementByPath("/Destination");
				MbElement httpEle = destEle.getFirstElementByPath("HTTP");
				httpEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"ReplyStatusCode", "404");				
			}
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
