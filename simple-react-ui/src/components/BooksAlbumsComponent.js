import React from 'react'
import BooksAlbumsService from '../services/BooksAlbumsService'

class BooksAlbumsComponent extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            items: []
        }
    }

    componentDidMount() {
        BooksAlbumsService.getBooksAndAlbums("Thank you scientist").then((response) => {
            this.setState({
                items: response.data.items
            })
        });
    }

    render() {
        return(
            <div>
                <h1 className = "text-center">Books and Albums</h1>
                <table className = "table table-striped">
                    <thead>
                        <tr>
                            <td>Title</td>
                            <td>Authors</td>
                            <td>Artist</td>
                            <td>Book or Album</td>
                        </tr>
                    </thead>
                    <tbody>
                     {
                            this.state.items.map(
                                item => 
                                <tr key = {item.title}>
                                    <td>{item.title}</td>
                                    <td>{item.authors}</td>
                                    <td>{item.artist}</td>
                                    <td>{item.book === true ? "Book" : "Album"}</td>
                                </tr>
                            )
                        } 
                    </tbody>
                </table>
            </div>
        )
    }
}

export default BooksAlbumsComponent