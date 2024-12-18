FROM nginx:latest

# Copy built Angular app from the first stage to the Nginx image
COPY ./target /usr/share/nginx/html

COPY ./html/en-US /usr/share/nginx/html/en-US
COPY ./html/icons /usr/share/nginx/html/icons
COPY ./html/img /usr/share/nginx/html/img
COPY ./html/logos /usr/share/nginx/html/logos
COPY ./html/malertservices /usr/share/nginx/html/malertservices
COPY ./html/telcofiles /usr/share/nginx/html/telcofiles
COPY ./html/404.html /usr/share/nginx/html/404.html

#COPY ./html/index.html /usr/share/nginx/html/index.html
COPY ./html/50x.html /usr/share/nginx/html/50x.html
COPY ./html/nginx-logo.png /usr/share/nginx/html/nginx-logo.png
COPY ./html/poweredby.png /usr/share/nginx/html/poweredby.png

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]