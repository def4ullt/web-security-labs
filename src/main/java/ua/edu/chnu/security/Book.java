package ua.edu.chnu.security;

import jakarta.persistence.*;

@Entity
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String title;
    public String author;
    protected Book() {}
    public Book(String title, String author) { this.title = title; this.author = author; }
}
