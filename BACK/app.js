var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
const uploadMiddleware = require('./util/uploadMiddleware')

var indexRouter = require('./rout/index');
var admUsers = require('./rout/authorised/routAdmUsers');
var manUsers = require('./rout/authorised/routManUsers');
var checkToken = require('./rout/authorised/routCheckToken')
var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use(uploadMiddleware.single('src'))

// app.use(function(req, res, next) {
//   console.log(req.url)
//   next(indexRouter)
// });

app.use('/', indexRouter);
app.use('/authorised/*', checkToken);
app.use('/authorised/admusers', admUsers);
app.use('/authorised/manusers', manUsers);
// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
