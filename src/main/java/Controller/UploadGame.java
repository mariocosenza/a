package Controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

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
				fileName = extractFileName(part);
			
				if (fileName != null && !fileName.equals("")) {
					 String sanitizedFileName = sanitizeFileName(fileName);
	                    if (isValidFileExtension(sanitizedFileName) && isValidFileContent(part)) {
	                        part.write(savePath + File.separator + sanitizedFileName);
	                        g1.setImg(sanitizedFileName);
	                        message += sanitizedFileName + "\n";
	                    } else {
	                        request.setAttribute("error", "Errore: File non valido");
	                        break;
	                    }
				} else {
					request.setAttribute("error", "Errore: Bisogna selezionare almeno un file");
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

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    private boolean isValidFileExtension(String fileName) {
        String[] allowedExtensions = { "jpg", "jpeg", "png", "gif" };
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        for (String ext : allowedExtensions) {
            if (ext.equals(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidFileContent(Part part) throws IOException {
        InputStream inputStream = part.getInputStream();
        byte[] header = new byte[8];
        inputStream.read(header);
        inputStream.close();

        return isJPEG(header) || isPNG(header) || isGIF(header);
    }
    
    private boolean isJPEG(byte[] header) {
        // JPEG files start with FF D8 FF
        return header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF;
    }

    private boolean isPNG(byte[] header) {
        // PNG files start with 89 50 4E 47 0D 0A 1A 0A
        return header[0] == (byte)0x89 && header[1] == (byte)0x50 && header[2] == (byte)0x4E && header[3] == (byte)0x47 &&
               header[4] == (byte)0x0D && header[5] == (byte)0x0A && header[6] == (byte)0x1A && header[7] == (byte)0x0A;
    }

    private boolean isGIF(byte[] header) {
        // GIF files start with GIF87a or GIF89a
        return (header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && 
                (header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a'));
    }
}
	
