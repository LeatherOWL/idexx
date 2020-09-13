import axios from 'axios'

const BOOKS_ALBUMS_REST_API_URL = 'http://localhost:8080/v1/booksAlbums'

class BooksAlbumsService {

    getBooksAndAlbums(data) {
        return axios.post(BOOKS_ALBUMS_REST_API_URL, data)
    }
}

export default new BooksAlbumsService()