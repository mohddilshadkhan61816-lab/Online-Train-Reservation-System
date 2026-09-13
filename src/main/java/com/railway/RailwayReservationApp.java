package com.railway;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/** Standalone Swing reservation application backed by a local SQLite database. */
public final class RailwayReservationApp {
    static final Color NAVY = new Color(16, 31, 57), BLUE = new Color(29, 78, 216), SKY = new Color(239, 246, 255);
    static final Color INK = new Color(25, 38, 60), MUTED = new Color(100, 116, 139), BORDER = new Color(218, 226, 238);
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    private final Database db = new Database();
    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            new RailwayReservationApp().start();
        });
    }

    private void start() {
        try { db.initialize(); } catch (SQLException e) { showError(null, "Unable to initialize the database: " + e.getMessage()); return; }
        showLogin();
    }

    private void showLogin() {
        frame = baseFrame("RailEase | Sign in", 1050, 650);
        JPanel root = new JPanel(new GridLayout(1, 2)); root.setBackground(Color.WHITE);
        JPanel brand = new JPanel(new GridBagLayout()); brand.setBackground(NAVY);
        JPanel brandText = new JPanel(); brandText.setOpaque(false); brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel("✦"); icon.setFont(new Font("SansSerif", Font.PLAIN, 58)); icon.setForeground(new Color(125, 211, 252)); icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = label("RailEase", 40, Color.WHITE, Font.BOLD); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = label("Travel, beautifully connected.", 17, new Color(203, 213, 225), Font.PLAIN); subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel note = label("Book journeys. Manage plans.\nAll in one calm, simple space.", 15, new Color(148, 163, 184), Font.PLAIN); note.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandText.add(icon); brandText.add(Box.createVerticalStrut(18)); brandText.add(title); brandText.add(Box.createVerticalStrut(10)); brandText.add(subtitle); brandText.add(Box.createVerticalStrut(35)); brandText.add(note); brand.add(brandText);

        JPanel side = new JPanel(new GridBagLayout()); side.setBackground(Color.WHITE);
        JPanel card = panel(); card.setPreferredSize(new Dimension(360, 400)); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel welcome = label("Welcome back", 27, INK, Font.BOLD); JLabel prompt = label("Sign in to plan your next journey.", 14, MUTED, Font.PLAIN);
        JTextField user = textField(); JPasswordField password = passwordField();
        card.add(welcome); card.add(Box.createVerticalStrut(7)); card.add(prompt); card.add(Box.createVerticalStrut(30));
        card.add(fieldLabel("USERNAME")); card.add(Box.createVerticalStrut(7)); card.add(user); card.add(Box.createVerticalStrut(18));
        card.add(fieldLabel("PASSWORD")); card.add(Box.createVerticalStrut(7)); card.add(password); card.add(Box.createVerticalStrut(25));
        JButton signIn = primaryButton("Sign in  →"); signIn.setAlignmentX(Component.LEFT_ALIGNMENT); card.add(signIn); card.add(Box.createVerticalStrut(20));
        JLabel demo = label("Demo: admin / admin123", 12, MUTED, Font.PLAIN); demo.setAlignmentX(Component.LEFT_ALIGNMENT); card.add(demo);
        ActionListener login = e -> {
            String username = user.getText().trim(); String pass = new String(password.getPassword());
            if (username.isEmpty() || pass.isEmpty()) { showError(frame, "Enter both username and password."); return; }
            try { if (db.authenticate(username, pass)) showDashboard(username); else showError(frame, "Access denied. Check your credentials and try again."); }
            catch (SQLException ex) { showError(frame, "Login could not be verified: " + ex.getMessage()); }
        };
        signIn.addActionListener(login); password.addActionListener(login); side.add(card); root.add(brand); root.add(side); frame.setContentPane(root); frame.setVisible(true);
    }

    private void showDashboard(String username) {
        frame.dispose(); frame = baseFrame("RailEase | Reservation desk", 1120, 730);
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(SKY);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(NAVY); header.setBorder(new EmptyBorder(16, 34, 16, 34));
        header.add(label("✦  RailEase", 23, Color.WHITE, Font.BOLD), BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0)); right.setOpaque(false); right.add(label("Signed in as " + username, 13, new Color(203,213,225), Font.PLAIN)); JButton out = ghostButton("Sign out"); right.add(out); header.add(right, BorderLayout.EAST); out.addActionListener(e -> { frame.dispose(); showLogin(); });
        JPanel main = new JPanel(new BorderLayout(0, 18)); main.setBackground(SKY); main.setBorder(new EmptyBorder(27, 34, 28, 34));
        JPanel heading = new JPanel(); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS)); heading.setOpaque(false); heading.add(label("Reservation desk", 28, INK, Font.BOLD)); heading.add(Box.createVerticalStrut(5)); heading.add(label("A smooth start to every journey.", 14, MUTED, Font.PLAIN)); main.add(heading, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("SansSerif", Font.BOLD, 14)); tabs.setBackground(Color.WHITE); tabs.addTab("  Book a journey  ", bookingPanel()); tabs.addTab("  Cancel a booking  ", cancellationPanel()); main.add(tabs, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH); root.add(main, BorderLayout.CENTER); frame.setContentPane(root); frame.setVisible(true);
    }

    private JPanel bookingPanel() {
        JPanel outer = new JPanel(new GridBagLayout()); outer.setBackground(Color.WHITE); outer.setBorder(new EmptyBorder(23, 28, 23, 28));
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints c = gbc(); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        JTextField passenger = textField(), trainNo = textField(), trainName = textField(), date = textField(), source = textField(), destination = textField();
        trainName.setEditable(false); trainName.setBackground(new Color(248,250,252));
        JComboBox<String> travelClass = comboBox(new String[]{"Select class", "First AC (1A)", "Second AC (2A)", "Third AC (3A)", "Sleeper (SL)", "Second Sitting (2S)"});
        addField(form, c, 0, 0, "PASSENGER NAME", passenger); addField(form, c, 1, 0, "TRAIN NUMBER", trainNo); addField(form, c, 0, 2, "TRAIN NAME", trainName); addField(form, c, 1, 2, "TRAVEL CLASS", travelClass);
        addField(form, c, 0, 4, "DATE OF JOURNEY", date); addField(form, c, 1, 4, "SOURCE STATION", source); addField(form, c, 0, 6, "DESTINATION STATION", destination);
        JLabel hint = label("Format: DD-MM-YYYY  •  Example: 28-10-2026", 12, MUTED, Font.PLAIN); c.gridx=0;c.gridy=8;c.gridwidth=2;c.insets=new Insets(4,0,17,0);form.add(hint,c);
        JButton book = primaryButton("Confirm booking  →"); c.gridy=9;c.insets=new Insets(0,0,0,0); c.anchor=GridBagConstraints.WEST; form.add(book,c);
        trainNo.getDocument().addDocumentListener(new SimpleDocumentListener(() -> { try { trainName.setText(db.trainName(trainNo.getText().trim())); } catch (SQLException ignored) { trainName.setText(""); } }));
        book.addActionListener(e -> {
            String p = passenger.getText().trim(), no=trainNo.getText().trim(), name=trainName.getText().trim(), d=date.getText().trim(), s=source.getText().trim(), dest=destination.getText().trim();
            if (p.isEmpty()||no.isEmpty()||name.isEmpty()||d.isEmpty()||s.isEmpty()||dest.isEmpty()||travelClass.getSelectedIndex()==0) { showError(frame,"Please complete every booking field. Use a listed train number."); return; }
            if (!no.matches("\\d+")) { showError(frame,"Train number must contain digits only."); return; }
            try { LocalDate.parse(d, DATE_FORMAT); } catch (DateTimeParseException ex) { showError(frame,"Use a valid date in DD-MM-YYYY format."); return; }
            try { Booking b = db.book(p,no,name,(String)travelClass.getSelectedItem(),d,s,dest); showBookingConfirmation(b); passenger.setText(""); trainNo.setText(""); date.setText(""); source.setText(""); destination.setText(""); travelClass.setSelectedIndex(0); }
            catch (SQLException ex) { showError(frame,"The ticket could not be saved: " + ex.getMessage()); }
        });
        outer.add(form); return outer;
    }

    private JPanel cancellationPanel() {
        JPanel outer = new JPanel(new GridBagLayout()); outer.setBackground(Color.WHITE); outer.setBorder(new EmptyBorder(40, 28, 28, 28));
        JPanel box = new JPanel(); box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS)); box.setOpaque(false); box.setPreferredSize(new Dimension(620, 450));
        box.add(label("Cancel with confidence", 25, INK, Font.BOLD)); box.add(Box.createVerticalStrut(7)); box.add(label("Enter the PNR to review a reservation before cancelling it.",14,MUTED,Font.PLAIN)); box.add(Box.createVerticalStrut(28));
        JTextField pnr = textField(); JPanel line=new JPanel(new BorderLayout(10,0));line.setOpaque(false);line.add(pnr,BorderLayout.CENTER); JButton fetch=secondaryButton("Fetch booking");line.add(fetch,BorderLayout.EAST);box.add(fieldLabel("PNR NUMBER"));box.add(Box.createVerticalStrut(7));box.add(line);box.add(Box.createVerticalStrut(25));
        JPanel detail=panel();detail.setLayout(new BoxLayout(detail,BoxLayout.Y_AXIS));detail.setVisible(false); JButton cancel=dangerButton("Cancel this booking"); cancel.setAlignmentX(Component.LEFT_ALIGNMENT); detail.add(cancel); box.add(detail);
        final Booking[] selected={null}; fetch.addActionListener(e -> { try { Booking b=db.find(pnr.getText().trim()); if(b==null){detail.setVisible(false);showError(frame,"No reservation was found for that PNR.");return;} selected[0]=b; detail.removeAll(); detail.add(bookingDetails(b)); detail.add(Box.createVerticalStrut(18)); detail.add(cancel); detail.revalidate();detail.setVisible(true); } catch(SQLException ex){showError(frame,"Could not retrieve the reservation: "+ex.getMessage());} });
        cancel.addActionListener(e -> { if(selected[0]==null)return; int choice=JOptionPane.showConfirmDialog(frame,"Cancel reservation " + selected[0].pnr + " for " + selected[0].passenger + "?\nThis action cannot be undone.","Confirm cancellation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE); if(choice==JOptionPane.YES_OPTION)try{db.cancel(selected[0].pnr);detail.setVisible(false);pnr.setText("");selected[0]=null;JOptionPane.showMessageDialog(frame,"Reservation cancelled successfully.","Booking cancelled",JOptionPane.INFORMATION_MESSAGE);}catch(SQLException ex){showError(frame,"Cancellation failed: "+ex.getMessage());} });
        outer.add(box);return outer;
    }

    private void showBookingConfirmation(Booking b) { JOptionPane.showMessageDialog(frame, bookingDetails(b), "Journey confirmed · PNR " + b.pnr, JOptionPane.INFORMATION_MESSAGE); }
    private JPanel bookingDetails(Booking b) { JPanel d=new JPanel(new GridLayout(0,2,16,9));d.setOpaque(false); String[][] rows={{"PNR",b.pnr},{"Passenger",b.passenger},{"Train",b.trainNo+" · "+b.trainName},{"Class",b.travelClass},{"Journey date",b.date},{"Route",b.source+" → "+b.destination}};for(String[] r:rows){d.add(label(r[0].toUpperCase(),11,MUTED,Font.BOLD));d.add(label(r[1],13,INK,Font.PLAIN));}return d; }
    private JFrame baseFrame(String title,int w,int h){JFrame f=new JFrame(title);f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);f.setSize(w,h);f.setMinimumSize(new Dimension(900,600));f.setLocationRelativeTo(null);return f;}
    private static JPanel panel(){JPanel p=new JPanel();p.setBackground(Color.WHITE);p.setBorder(new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(20,22,20,22)));return p;}
    private static JLabel label(String s,int size,Color col,int style){JLabel l=new JLabel("<html>"+s.replace("\n","<br>")+"</html>");l.setFont(new Font("SansSerif",style,size));l.setForeground(col);return l;}
    private static JLabel fieldLabel(String s){return label(s,11,MUTED,Font.BOLD);}
    private static JTextField textField(){JTextField f=new JTextField();f.setFont(new Font("SansSerif",Font.PLAIN,15));f.setPreferredSize(new Dimension(250,42));f.setBorder(new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(0,11,0,11)));return f;}
    private static JPasswordField passwordField(){JPasswordField f=new JPasswordField();f.setFont(new Font("SansSerif",Font.PLAIN,15));f.setPreferredSize(new Dimension(250,42));f.setBorder(new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(0,11,0,11)));return f;}
    private static JComboBox<String> comboBox(String[] values){JComboBox<String> b=new JComboBox<>(values);b.setFont(new Font("SansSerif",Font.PLAIN,14));b.setPreferredSize(new Dimension(250,42));b.setBorder(new LineBorder(BORDER));return b;}
    private static JButton button(String s,Color bg,Color fg){JButton b=new JButton(s);b.setFont(new Font("SansSerif",Font.BOLD,14));b.setForeground(fg);b.setBackground(bg);b.setFocusPainted(false);b.setBorder(new EmptyBorder(11,19,11,19));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private static JButton primaryButton(String s){return button(s,BLUE,Color.WHITE);} private static JButton secondaryButton(String s){return button(s,new Color(224,231,255),new Color(49,46,129));} private static JButton dangerButton(String s){return button(s,new Color(220,38,38),Color.WHITE);} private static JButton ghostButton(String s){return button(s,new Color(30,58,95),Color.WHITE);}
    private static GridBagConstraints gbc(){GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(0,0,7,18);return c;} private static void addField(JPanel p,GridBagConstraints c,int x,int y,String label,JComponent field){c.gridx=x;c.gridy=y;c.gridwidth=1;c.insets=new Insets(0,0,7,18);p.add(fieldLabel(label),c);c.gridy=y+1;c.insets=new Insets(0,0,18,18);p.add(field,c);}
    private static void showError(Component parent,String message){JOptionPane.showMessageDialog(parent,message,"RailEase",JOptionPane.ERROR_MESSAGE);}

    /** Immutable reservation details, written as a regular class for Java 8 support. */
    static final class Booking {
        final String pnr;
        final String passenger;
        final String trainNo;
        final String trainName;
        final String travelClass;
        final String date;
        final String source;
        final String destination;

        Booking(String pnr, String passenger, String trainNo, String trainName,
                String travelClass, String date, String source, String destination) {
            this.pnr = pnr;
            this.passenger = passenger;
            this.trainNo = trainNo;
            this.trainName = trainName;
            this.travelClass = travelClass;
            this.date = date;
            this.source = source;
            this.destination = destination;
        }
    }
    @FunctionalInterface interface Change { void run(); } static class SimpleDocumentListener implements javax.swing.event.DocumentListener { final Change change; SimpleDocumentListener(Change c){change=c;}public void insertUpdate(javax.swing.event.DocumentEvent e){change.run();}public void removeUpdate(javax.swing.event.DocumentEvent e){change.run();}public void changedUpdate(javax.swing.event.DocumentEvent e){change.run();} }
    static final class Database {
        private static final String URL="jdbc:sqlite:railease.db";
        private Connection connect() throws SQLException{return DriverManager.getConnection(URL);}
        void initialize() throws SQLException {try(Connection c=connect();Statement s=c.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT NOT NULL)");s.executeUpdate("CREATE TABLE IF NOT EXISTS trains (number TEXT PRIMARY KEY, name TEXT NOT NULL)");s.executeUpdate("CREATE TABLE IF NOT EXISTS reservations (pnr TEXT PRIMARY KEY, passenger TEXT NOT NULL, train_no TEXT NOT NULL, train_name TEXT NOT NULL, class_type TEXT NOT NULL, journey_date TEXT NOT NULL, source TEXT NOT NULL, destination TEXT NOT NULL)");try(PreparedStatement u=c.prepareStatement("INSERT OR IGNORE INTO users VALUES (?,?)");PreparedStatement t=c.prepareStatement("INSERT OR IGNORE INTO trains VALUES (?,?)")){for(String[] x:new String[][]{{"admin","admin123"},{"traveler","welcome123"}}){u.setString(1,x[0]);u.setString(2,x[1]);u.executeUpdate();}for(String[] x:new String[][]{{"12002","Bhopal Shatabdi"},{"12952","Mumbai Rajdhani"},{"12301","Howrah Rajdhani"},{"12627","Karnataka Express"}}){t.setString(1,x[0]);t.setString(2,x[1]);t.executeUpdate();}}}}
        boolean authenticate(String username,String password)throws SQLException{try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=?")){p.setString(1,username);p.setString(2,password);return p.executeQuery().next();}}
        String trainName(String number)throws SQLException{if(!number.matches("\\d+"))return "";try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT name FROM trains WHERE number=?")){p.setString(1,number);ResultSet r=p.executeQuery();return r.next()?r.getString(1):"";}}
        Booking book(String passenger,String no,String name,String cls,String date,String source,String destination)throws SQLException{String pnr="RE"+System.currentTimeMillis()+String.format("%03d",new Random().nextInt(1000));try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO reservations VALUES (?,?,?,?,?,?,?,?)")){String[] a={pnr,passenger,no,name,cls,date,source,destination};for(int i=0;i<a.length;i++)p.setString(i+1,a[i]);p.executeUpdate();return new Booking(pnr,passenger,no,name,cls,date,source,destination);}}
        Booking find(String pnr)throws SQLException{try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM reservations WHERE pnr=?")){p.setString(1,pnr);ResultSet r=p.executeQuery();return r.next()?new Booking(r.getString("pnr"),r.getString("passenger"),r.getString("train_no"),r.getString("train_name"),r.getString("class_type"),r.getString("journey_date"),r.getString("source"),r.getString("destination")):null;}}
        void cancel(String pnr)throws SQLException{try(Connection c=connect();PreparedStatement p=c.prepareStatement("DELETE FROM reservations WHERE pnr=?")){p.setString(1,pnr);p.executeUpdate();}}
    }
}
