package com.ibm.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class Book {

	private static HashMap<Integer,Book> books = new HashMap<Integer,Book>();
	
	private static Integer current = 0;
	private Integer id = null;
	private String title = null;
	private String author = null;
	private String isbn = null;
	private Date published = null;
	private String language = null;
	private List<String> formats = new ArrayList<String>();
	private static DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

	
	static {
		Book book1 = new Book("Steve Jobs", "1451648537", "Walter Isaacson", "English", new GregorianCalendar(2011, 10, 24).getTime());
		book1.addFormat("Hardcover");
		book1.addFormat("Paperback");
		book1.addFormat("Audiobook CD");
		book1.addFormat("Audible");
		addBook(book1);
		
		Book book2 = new Book("Beautiful Whale", "1419703846", "Bryant Austin, Sylvia Earle", "English", new GregorianCalendar(2013, 4, 2).getTime());
		book2.addFormat("Hardcover");
		addBook(book2);

		Book book3 = new Book("A Game of Thrones (A Song of Ice and Fire, Book 1)", "9780553103547", "George R. R. Martin", "English", new GregorianCalendar(1996, 8, 1).getTime());
		book3.addFormat("Kindle");
		book3.addFormat("Hardcover");
		book3.addFormat("Paperback");
		book3.addFormat("Audiobook CD");
		book3.addFormat("Audible");
		addBook(book3);
	}
	
	public Book() {
		
	}
	
	public Book(String title, String isbn,  String author, String language, Date published) {
		this.title = title;
		this.isbn = isbn;
		this.author = author;
		this.language = language;
		this.published = published;
	}
	
	public static List<Book> list() {
		Collection<Book> collection = books.values();
		List<Book> list = null;
		if (collection instanceof List)
		  list = (List<Book>)collection;
		else
		  list = new ArrayList<Book>(collection);
		return list;
	}
	
	public static Book getBookById(Integer id) {
		return books.get(id);
	}
	
	public static synchronized Book addBook(Book book) {
		current = current + 1;
		book.setId(current);
		books.put(current, book);
		return book;
	}
	
	public static synchronized void updateBook(Integer id, Book thisBook) {
		Book book = books.get(id);
		book.title = thisBook.title;
		book.isbn = thisBook.isbn;
		book.author = thisBook.author;
		book.published = thisBook.published;
		book.formats = thisBook.formats;		
	}
	
	public static synchronized void removeBook(Integer id) {
		books.remove(id);
	}
	
// ----- 
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public Date getPublished() {
		return published;
	}
	
	public String getPublishedAsString() {
		//DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
		return dateFormat.format(published);
	}
	
	public void setPublishedAsString(String strDate) throws ParseException {
		this.published = dateFormat.parse(strDate);
	}

	public void setPublished(Date published) {
		this.published = published;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<String> getFormats() {
		return formats;
	}

	public void setFormats(List<String> formats) {
		this.formats = formats;
	}
	
	public void addFormat(String format) {
		this.formats.add(format);
	}
	
	public void removeAllFormats() {
		this.formats.removeAll(this.formats);
	}

	
}
