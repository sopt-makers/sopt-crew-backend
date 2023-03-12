FROM nginx
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
VOLUME [ "/var/log/nginx/log" ]
EXPOSE 80
EXPOSE 443
EXPOSE 3000
CMD [ "nginx", "-g", "daemon off;" ]