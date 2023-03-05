package com.xianj.community.entity;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
@Service
public class listener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("now time " + Instant.ofEpochMilli(e.getWhen()));
        Toolkit.getDefaultToolkit().beep();
    }
}
