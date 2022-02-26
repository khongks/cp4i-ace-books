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

public class PostBooks_JavaCompute extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		//MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage();
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ----------------------------------------------------------
			// Add user code below
			MbElement dataEle = inMessage.getRootElement().getFirstElementByPath("/JSON/Data");
			MbElement titleEle = dataEle.getFirstElementByPath("Title");
			MbElement isbnEle = dataEle.getFirstElementByPath("ISBN");
			MbElement authorEle = dataEle.getFirstElementByPath("Author");
			MbElement publishedEle = dataEle.getFirstElementByPath("Published");
			MbElement languageEle = dataEle.getFirstElementByPath("Language");
			MbElement formatsEle = dataEle.getFirstElementByPath("Formats");
			
			Book book = new Book();
			book.setTitle(titleEle.getValueAsString());
			book.setIsbn(isbnEle.getValueAsString());
			book.setAuthor(authorEle.getValueAsString());
			book.setLanguage(languageEle.getValueAsString());
			book.setPublishedAsString(publishedEle.getValueAsString());
			if(formatsEle != null) {
				MbElement elem = formatsEle.getFirstChild();
				do {
					book.addFormat(elem.getValueAsString());
					elem = elem.getNextSibling();
				} while (elem != null);
			}
			Book newBook = Book.addBook(book);
			// HTTP Status Code
			MbMessage localEnvironment = inAssembly.getLocalEnvironment();
			MbElement destEle = localEnvironment.getRootElement().getFirstElementByPath("/Destination");
			MbElement httpEle = destEle.getFirstElementByPath("HTTP");
			httpEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"ReplyStatusCode", "201");
			// JSON Body
			MbElement jsonEle = outMessage.getRootElement().createElementAsLastChild(MbJSON.PARSER_NAME);
			MbElement bookEle = jsonEle.createElementAsLastChild(MbElement.TYPE_NAME, MbJSON.DATA_ELEMENT_NAME, null);
			bookEle.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Id", newBook.getId().toString());
			
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
