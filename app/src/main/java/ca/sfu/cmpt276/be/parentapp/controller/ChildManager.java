package ca.sfu.cmpt276.be.parentapp.controller;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;

import ca.sfu.cmpt276.be.parentapp.model.Child;

/**
 * ChildManager gets the Child ArrayList from DataManager and can modify it with setters and
 * getters. It is also iterable.
 */
public class ChildManager implements Iterable<Child> {
    private final ArrayList<Child> allChildren = DataManager.getInstance().getChildList();


    public Child get(int index) {
        return allChildren.get(index);
    }

    public String getName(int index) {
        return this.get(index).getName();
    }

    public void add(Child addThis) {
        allChildren.add(addThis);
        saveList();
    }

    public void remove(int index) {
        allChildren.remove(index);
        saveList();

    }

    public void edit(int index, String editName) {
        allChildren.get(index).setName(editName);
        saveList();
    }

    public  ArrayList<Child> getAll() {
        return allChildren;
    }

    public int size() {
        return allChildren.size();
    }

    public void saveList() {
        DataManager.getInstance().serializeChildren();
    }

    public boolean isEmpty() {
        return allChildren.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<Child> iterator() {
        return allChildren.iterator();
    }
}