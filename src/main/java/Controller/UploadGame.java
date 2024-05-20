package Controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import model.ProductModel;
import model.game;



/**
 * Servlet implementation class AddGame
 */
@WebServlet("/AddGame")
@MultipartConfig()
public class UploadGame extends HttpServlet {
	
	private boolean isValidImageFile(Part part) {
	    try {
	        InputStream is = part.getInputStream();
	        byte[] bytes = new byte[4];
	        is.read(bytes, 0, 4);
	        is.close();

	        // Magic numbers per JPEG (FF D8 FF) e PNG (89 50 4E 47)
	        byte[] jpegMagic = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
	        byte[] pngMagic = {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47};

	        // Controlla se i primi byte corrispondono ai magic numbers
	        if (Arrays.equals(Arrays.copyOfRange(bytes, 0, 3), jpegMagic) ||
	            Arrays.equals(bytes, pngMagic)) {
	            return true;
	        } else {
	            return false;
	        }
	    } catch (IOException e) {
	        return false;
	    } 
	}
	
	private static final long serialVersionUID = 1L;
	static String SAVE_DIR = "img";
	static ProductModel GameModels = new ProductModelDM();
	
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	LocalDateTime now = LocalDateTime.now();
	
	
    public UploadGame() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");

		out.write("Error: GET method is used but POST method is required");
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Collection<?> games = (Collection<?>) request.getSession().getAttribute("games");
		String savePath = request.getServletContext().getRealPath("") + File.separator + SAVE_DIR;
		game g1 = new game();
		/*
		File fileSaveDir = new File(savePath);
		
		 * if (!fileSaveDir.exists()) { fileSaveDir.mkdir(); }
		 */
		String fileName= null;
		String message = "upload =\n";
		if (request.getParts() != null && request.getParts().size() > 0) {
			for (Part part : request.getParts()) {
		        if (isValidImageFile(part)) {
		            fileName = extractFileName(part);
		            if (fileName != null && !fileName.equals("")) {
		                part.write(savePath + File.separator + fileName);
		                g1.setImg(fileName);
		                message = message + fileName + "\n";
		            } else {
		                request.setAttribute("error", "Errore: Bisogna selezionare almeno un file");
		            }
		        } else {
		            request.setAttribute("error", "Errore: Il file caricato non è un'immagine valida");
		        }
		    }
		}
		
		g1.setName(request.getParameter("nomeGame"));
		g1.setYears(request.getParameter("years"));
		g1.setAdded(dtf.format(now));
		g1.setQuantity(Integer.valueOf(request.getParameter("quantita")));
		g1.setPEG(Integer.valueOf(request.getParameter("PEG")));
		g1.setIva(Integer.valueOf(request.getParameter("iva")));
		g1.setGenere(request.getParameter("genere"));
		g1.setDesc(request.getParameter("desc"));
		g1.setPrice(Float.valueOf(request.getParameter("price")));
		
		try {
			GameModels.doSave(g1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//request.setAttribute("message", message);
		request.setAttribute("stato", "success!");
		
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/gameList?page=admin&sort=added DESC");
		dispatcher.forward(request, response);
	}
	private String extractFileName(Part part) {
		// content-disposition: form-data; name="file"; filename="file.txt"
		String contentDisp = part.getHeader("content-disposition");
	    String[] items = contentDisp.split(";");
	    for (String s : items) {
	        if (s.trim().startsWith("filename")) {
	            return s.substring(s.indexOf("=") + 2, s.length() - 1);
	        }
	    }
	    return "";
	}
	

}
