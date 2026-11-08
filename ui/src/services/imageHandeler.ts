export class imageHandeler {
    static async byteArrayToBase64(byteArray: string | Uint8Array | number[]) {
        if (typeof byteArray === 'string') {
            return byteArray;
        }

        const bytes = new Uint8Array(byteArray);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary);
    }
}