package com.rail.feedback;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.mail.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Text;

/**
 * Servlet implementation class Feedback
 */
public class Feedback extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Feedback() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		//System.out.println("Calling Get ..with tripId : "+tripId);
		String result = "";
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		result = getHead() ;
		
		String tripId = request.getParameter("TripID");
		if(tripId!=null)
		{
			result+= getFeedbackForTrip(datastore, tripId);
		}
		else
		{
			result+=getTripInformation(datastore);
		}
		 result = result + getRestOfBottom();
		 
		 response.getWriter().append(result).append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Calling POST ..");
		//String PSI = request.getParameter("PSI");
		//String CoachNumber = request.getParameter("CoachNumber");
		//String SeatNumber = request.getParameter("SeatNumber");
		//String MobileNumber = request.getParameter("MobileNumber");
		// TrainNumber = request.getParameter("TrainNumber");	
			// Storing Data in data store
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Saving image
		//String fromClientphoto= request.getParameter("photo");
		//byte[] b = fromClientphoto.getBytes();

		//FileOutputStream fos = new FileOutputStream("C:\\img.png");
		//fos.write(b);
		Text photoText = new Text(request.getParameter("photo"));
		String photoString = request.getParameter("photo");
		System.out.println("String  : "+photoString);
		System.out.println("String length : "+photoString.length());
		//Text nakedQuery = new Text(request.getQueryString());
		// Saving Feedback Details
		Entity feedback = new Entity("Feedback");
		//Entity feedback = new Entity("Ttemp");
		feedback.setProperty("TripID", request.getParameter("TripID"));
		feedback.setProperty("PNRNumber", request.getParameter("PNRNumber"));
		feedback.setProperty("CoachNumber", request.getParameter("CoachNumber"));
		feedback.setProperty("SeatNumber", request.getParameter("SeatNumber"));
		feedback.setProperty("MobileNumber", request.getParameter("MobileNumber"));
		feedback.setProperty("TrainNumber", request.getParameter("TrainNumber"));
		feedback.setProperty("Date", request.getParameter("Date"));
		feedback.setProperty("PSI", request.getParameter("PSI"));
		feedback.setProperty("Photo",photoText);
		//feedback.setProperty("nakedQuery",nakedQuery);
		datastore.put(feedback);
		
		//Filter propertyFilter =new FilterP("TripID", FilterOperator.EQUAL, request.getParameter("TripID"));
		Query query = new Query("Trip").addFilter("TripID", FilterOperator.EQUAL, request.getParameter("TripID"));
		// List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		 List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		
		if(results.size()==0)
		{
			Entity trip = new Entity("Trip");
			trip.setProperty("TripID", request.getParameter("TripID"));
			trip.setProperty("TrainNumber", request.getParameter("TrainNumber"));
			trip.setProperty("Date", request.getParameter("Date"));
			datastore.put(trip);
		}
		response.getWriter().append("Served Responded : Success").append(request.getContextPath());
		//doGet(request, response);
	}
	private String getHead()
	{
		return "<!DOCTYPE html><html lang=\"en\"><head><title>Feedback Results</title><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\"><script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script><script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script></head><body>";
	}
	
	private String getFeedbackTableHead(String tripId)
	{
		return "<div class=\"container\"><h2> <a href=\"?\"><span class=\"glyphicon glyphicon-chevron-left\"></span></a>  Feedback Results</h2><p>This table contains Feedback of Trip ID : "+tripId+"</p>    <table class=\"table\"><thead><tr><th>PNR Number</th><th>Mobile Number</th><th>Coach Number</th><th>Seat Number</th><th>PSI</th><th>Photo</th></tr></thead><tbody>";
	}
	private String getTripTableHead()
	{
		return "<div class=\"container\"><h2>Trip Results</h2><p>This table contains all the trip details </p> <table class=\"table\"><thead><tr><th>Trip ID</th><th>Train Number</th><th>Train Start Date</th></tr></thead><tbody>";
	}
	
	private String getRestOfBottom()
	{
		return " </tbody></table></div></body></html>";
	}
	
	public String getTripInformation(DatastoreService datastore)
	{
		 Query query = new Query("Trip");
		 List<Entity> results = datastore.prepare(query).asList(
					FetchOptions.Builder.withDefaults());
		 String result =  getTripTableHead();
		 if(results.size()!=0)
		 {
			 for (Entity entity : results) {
					String tripId = (String) entity.getProperty("TripID");
					String trainNumber = (String) entity.getProperty("TrainNumber");
					String trainStartDate = (String) entity.getProperty("Date");
					result = result + "<tr ><td><a href='?TripID="+tripId+"'>"+tripId+"</a></td><td>"+trainNumber+"</td><td>"+trainStartDate+"</td></tr>";
				}
		 }
		 return result;
	}
	
	private String getFeedbackForTrip(DatastoreService datastore,String tripID)
	{
		String result = getFeedbackTableHead(tripID);
		 //Query query = new Query("Feedback");
		Query query = new Query("Feedback").addFilter("TripID", FilterOperator.EQUAL, tripID);
		 List<Entity> results = datastore.prepare(query).asList(
					FetchOptions.Builder.withDefaults());
		 //result = getHead() + getTableHead();
		 if(results.size()!=0)
		 {
			 int totalPsi = 0;
			 for (Entity entity : results) {
					String psi = (String) entity.getProperty("PSI");
					String PNRNumber = (String) entity.getProperty("PNRNumber");
					String coachNumber = (String) entity.getProperty("CoachNumber");
					String SeatNumber = (String) entity.getProperty("SeatNumber");
					String MobileNumber = (String) entity.getProperty("MobileNumber");
					String Date = (String) entity.getProperty("Date");
					
					Text Photo = (Text) entity.getProperty("Photo");
                    String photoString = Photo.getValue();
                    
                    byte[] decodedImageByte = decodeUrlSafe(photoString.getBytes());
                    
                    String decodedString =new String(decodedImageByte);
                    System.out.println(decodedString);
					//result = result + "</br>"+coachNumber+" | "+seatNumber+" | "+psi;
					double psiInt = Double.parseDouble(psi);
					totalPsi+=psiInt;
					result = result + "<tr><td>"+PNRNumber+"</td>  <td>"+MobileNumber+"</td> <td>"+coachNumber+"</td><td>"+SeatNumber+"</td><td>"+psi+"</td><td><img height='150px' width='200px' src=\"data:image/png;base64,"+decodedString+"\"/></td></tr>";
				}
			 result+="<tr><td></td><td></td><td></td><td><b>Total PSI</b></td><td><b>"+totalPsi+"</b></td>";
			 result+="<tr><td></td><td></td><td></td><td><b>Average PSI</b></td><td><b>"+totalPsi/results.size()+"</b></td>";
			
		 }
		 return result;
	}
	
	public static byte[] decodeUrlSafe(byte[] data) {
	    byte[] encode = Arrays.copyOf(data, data.length);
	    for (int i = 0; i < encode.length; i++) {
	        if (encode[i] == '-') {
	            encode[i] = '+';
	        } else if (encode[i] == '_') {
	            encode[i] = '/';
	        }
	    }
	   // return Base64.decode(encode);
	    return encode;
	}
	
}
