const multer = require('multer')

const storage = multer.diskStorage({
    destination(req, file, cb) {
        cb(null, 'C:/GitLab/miskaz/MWP/MWPBack/upload_dir')
    },
    filename(req, file, cb) {
        cb(null, file.originalname)
    }
})

const allowedTypes = ['image/png', 'image/jpg', 'image/jpeg', 'application/pdf']
const fileFilter = (req, file, cb) => {
    if (allowedTypes.includes(file.mimeType)) {
        cb(null, true)
    } else {
        cb(null, false)
    }
    cb(null, true)
}

module.exports = multer({storage, fileFilter})
