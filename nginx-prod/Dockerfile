FROM nginx:latest



COPY ./nginx /usr/share/nginx

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]